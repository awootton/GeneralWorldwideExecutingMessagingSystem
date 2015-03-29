package org.messageweb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.messageweb.impl.JedisRedisPubSubImpl;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.util.PubSub;
import org.messageweb.util.TimeoutCache;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

/**
 * TODO: make an interface and an impl. Hide the helper classes.
 * 
 * @author awootton
 *
 */
public class ServerGlobalState {

	public static Logger logger = Logger.getLogger(ServerGlobalState.class);

	ExecutorService executor = null;

	public String id = "Server" + ("" + Math.random()).substring(2);
	// FIXME: more random here

	MyWebSocketServer server;

	public TimeoutCache timeoutCache;

	Thread serverThread;

	PubSub redis;

	ClusterState cluster;

	public AmazonDynamoDBClient dynamo;
	public DynamoDBMapper mapper;

	public ServerGlobalState(int port, ClusterState cluster) {

		this.cluster = cluster;

		// executor = Executors.newCachedThreadPool(new MyThreadFactory());
		executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new MyThreadFactory());

		// open the port
		server = new MyWebSocketServer(this);
		Runnable starter = server.new Starter(port, this);
		serverThread = new Thread(starter);
		serverThread.setName("id");
		serverThread.setDaemon(true);
		serverThread.start();

		timeoutCache = new TimeoutCache(executor, id);

		redis = new JedisRedisPubSubImpl("localhost", new LocalSubHandler());

		cluster.name2server.put(id, this);

		AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
				new ClasspathPropertiesFileCredentialsProvider());

		chain = new DefaultAWSCredentialsProviderChain();

		dynamo = new AmazonDynamoDBClient(chain);

		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));

		mapper = new DynamoDBMapper(dynamo);

		// how do we wait for server to start up?
		while (server.startedChannel == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	private class MyThreadFactory implements ThreadFactory {

		int count = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			String name = "GWEMS" + cluster.name + ":" + id + ":" + count++;
			thread.setName(name);
			return thread;
		}

	}

	private class GlobalStateSetter implements Runnable {
		Runnable child;

		public GlobalStateSetter(Runnable child) {
			super();
			this.child = child;
		}

		@Override
		public void run() {
			// set the global in ThreadLocal
			ServerGlobalState global = ServerGlobalState.this;
			myGlobalStateObj.set(global);
			child.run();
			myGlobalStateObj.set(null);
		}
	}

	public void execute(Runnable r) {
		executor.execute(new GlobalStateSetter(r));
	}

	private static final ThreadLocal<ServerGlobalState> myGlobalStateObj = new ThreadLocal<ServerGlobalState>();

	private static final ThreadLocal<ChannelHandlerContext> myChannelContextStateObj = new ThreadLocal<ChannelHandlerContext>();

	/**
	 * Incoming messages from the WS server, or incoming in general, come directly through here.
	 * 
	 * @param ctx
	 * @param child
	 */
	public void executeChannelMessage(ChannelHandlerContext ctx, String message) {
		if (logger.isTraceEnabled()) {
			logger.trace("Sending message to execute on CtxWrapper with " + message);
		}
		execute(new CtxWrapper(ctx, message));
	}

	public void stop() {
		server.stop();
		executor.shutdown();
	}

	/**
	 * For sending reply messages on WS sockets
	 * 
	 * @return
	 */
	public static ChannelHandlerContext getCtx() {
		return myChannelContextStateObj.get();
	}

	/**
	 * For knowing which server we on a member of in multi-server and multi-pool simulations.
	 * 
	 * @return
	 */
	public static ServerGlobalState getGlobal() {
		return myGlobalStateObj.get();
	}

	private static class CtxWrapper implements Runnable {

		ChannelHandlerContext ctx;
		String message;

		public CtxWrapper(ChannelHandlerContext ctx, String message) {
			super();
			this.ctx = ctx;
			this.message = message;
		}

		@Override
		public void run() {
			myChannelContextStateObj.set(ctx);
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("CtxWrapper deserialize  " + message);
				}
				Runnable r = ServerGlobalState.deserialize(message);
				if (logger.isTraceEnabled()) {
					logger.trace("CtxWrapper running " + r);
				}
				r.run();
			} catch (JsonParseException e) {
				logger.error(message, e);
			} catch (JsonMappingException e) {
				logger.error(message, e);
			} catch (IOException e) {
				logger.error(message, e);
			}
			myChannelContextStateObj.set(null);
		}
	}

	private static final ThreadLocal<String> latestChannel = new ThreadLocal<String>();

	class LocalSubHandler implements PubSub.Handler {

		@Override
		public void handle(String channel, String message) {
			executor.execute(new ChannelWrapper(channel, message));
		}

	}

	private static class ChannelWrapper implements Runnable {

		String channel;
		String message;

		public ChannelWrapper(String channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			latestChannel.set(channel);
			try {
				Runnable runme = ServerGlobalState.deserialize(message);
				runme.run();
			} catch (JsonProcessingException e) {
				logger.error("bad message " + message, e);
			} catch (IOException e) {
				logger.error("bad message io " + message, e);
			}
			latestChannel.set("");

		}

	}

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {

		// MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@Class");
	}

	public static Runnable deserialize(String s) throws JsonParseException, JsonMappingException, IOException {
		Runnable obj = MAPPER.readValue(s, Runnable.class);
		return obj;
	}

	public static String serialize(Object message) throws JsonProcessingException {
		return MAPPER.writeValueAsString(message);
	}

	static public void reply(Runnable message) {
		ChannelHandlerContext ctx = myChannelContextStateObj.get();
		// System.out.println(" have ctx name = " + ctx.name());
		try {
			String sendme = ServerGlobalState.serialize(message);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			// e.printStackTrace();
			logger.error("bad message " + message, e);
		}

	}

}
