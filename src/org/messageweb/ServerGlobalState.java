package org.messageweb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.messageweb.impl.JedisRedisPubSubImpl;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.util.PubSub;
import org.messageweb.util.TimeoutCache;
import org.messageweb.util.TwoWayMapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

	private PubSub redis;

	ClusterState cluster;

	public DynamoHelper dynamoHelper;

	TwoWayMapping<Agent, String> session2channel = new TwoWayMapping<Agent, String>();

	public ServerGlobalState(int port, ClusterState cluster) {

		this.cluster = cluster;

		dynamoHelper = new DynamoHelper();

		executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new MyThreadFactory());

		// open the port
		server = new MyWebSocketServer(this);
		Runnable starter = server.new Starter(port, this);
		serverThread = new Thread(starter);
		serverThread.setName("id");
		serverThread.setDaemon(true);
		serverThread.start();

		timeoutCache = new TimeoutCache(executor, id);

		redis = new JedisRedisPubSubImpl("localhost", new LocalSubscriberHandler());

		cluster.name2server.put(id, this);

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
			ExecutionContext ec = context.get();
			ec.global = ServerGlobalState.this;
			child.run();
			ec.global = null;
		}
	}

	public void execute(Runnable r) {
		executor.execute(new GlobalStateSetter(r));
	}

	private static final ThreadLocal<ExecutionContext> context = new ThreadLocal<ExecutionContext>() {
		@Override
		protected ExecutionContext initialValue() {
			return new ExecutionContext();
		}
	};

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
		return context.get().ctx;
	}

	/**
	 * For knowing which server we on a member of in multi-server and multi-pool simulations.
	 * 
	 * @return
	 */
	public static ServerGlobalState getGlobal() {
		return context.get().global;
	}

	public static ExecutionContext getContext() {
		return context.get();
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
			ExecutionContext ec = context.get();
			ec.ctx = ctx;
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
			} finally {
				ec.ctx = null;
			}
		}
	}

	// private static final ThreadLocal<String> latestChannel = new ThreadLocal<String>();

	private class LocalSubscriberHandler implements PubSub.Handler {

		@Override
		public void handle(String channel, String message) {
			executor.execute(new ChannelSunscriberWrapper(channel, message));
		}
	}

	private class ChannelSunscriberWrapper implements Runnable {

		String channel;
		String message;

		public ChannelSunscriberWrapper(String channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			ExecutionContext ec = context.get();
			ec.subscribedChannel = channel;
			try {
				Runnable runme = ServerGlobalState.deserialize(message);
				// get all the local subscribers
				Set<Agent> agents = session2channel.thing2items_get(channel);
				for (Agent agent : agents) {
					// give them the message
					agent.messageQ.run(runme);
				}

			} catch (JsonProcessingException e) {
				logger.error("bad message " + message, e);
			} catch (IOException e) {
				logger.error("bad message io " + message, e);
			} finally {
				ec.subscribedChannel = "none";
			}
		}
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {

		// MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@Class");
	}
	private static final ObjectMapper plain_mapper = new ObjectMapper();

	public static Runnable deserialize(String s) throws JsonParseException, JsonMappingException, IOException {
		Runnable obj = MAPPER.readValue(s, Runnable.class);
		return obj;
	}

	public static String serialize(Object message) throws JsonProcessingException {
		return MAPPER.writeValueAsString(message);
	}

	public static String serializePretty(Object message) throws JsonProcessingException {
		return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(message);
	}

	public static ObjectNode serialize2node(Object message) throws IOException {

		Object obj = plain_mapper.valueToTree(message);
		return (ObjectNode) obj;

		// {
		// String sss = serialize(message);
		//
		//
		// JsonNode rootNode = plain_mapper.readTree(sss);
		// System.out.println(rootNode);
		//
		//
		// Object obj = plain_mapper.valueToTree(message);
		// System.out.println(obj);
		// }
		//
		//
		//
		// String sss = serialize(message);
		//
		// Map map = MAPPER.readValue(sss, Map.class);
		//
		// System.out.println(map);
		//
		// Object ooo = MAPPER.readTree(sss);
		// System.out.println(ooo);
		//
		// Object obj = MAPPER.valueToTree(message);
		// System.out.println(" 8888888888 " + obj);
		//
		//
		// String json = MAPPER.writeValueAsString(message);
		//
		// System.out.println("----" + json);
		//
		//
		// Object obje = MAPPER.readTree(json);
		//
		// System.out.println("----" + obje);
		//
		//
		//
		// // JsonNode nn = MAPPER.convertValue(message, JsonNode.class);
		//
		// // System.out.println(nn);
		//
		// ObjectNode node = MAPPER.createObjectNode();
		// // node = MAPPER.readTree(byte[])
		// // Object obje = MAPPER.valueToTree(message);
		// // System.out.println(obje);
		// return node;
	}

	static public void reply(Runnable message) {
		ChannelHandlerContext ctx = context.get().ctx;
		// System.out.println(" have ctx name = " + ctx.name());
		try {
			String sendme = ServerGlobalState.serialize(message);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			// e.printStackTrace();
			logger.error("bad message " + message, e);
		}

	}

	public void publish(String channel, String message) {
		redis.publish(channel, message);
		// tell the local subscribers
		executor.execute(new ChannelSunscriberWrapper(channel, message));
	}

	public void subscribe(Agent agent, String channel) {
		// TDB
		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty()) {
			redis.subcribe(channel);
		}
		session2channel.add(agent, channel);
	}

	public void unsubscribe(Agent agent, String channel) {
		session2channel.remove(agent, channel);
		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty()) {
			redis.unsubcribe(channel);
		}
	}

}
