package org.messageweb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.messageweb.impl.JedisRedisPubSubImpl;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.util.PubSub;
import org.messageweb.util.TimeoutCache;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

/** TODO: make an interface and an impl. Hide the helper classes.
 * 
 * @author awootton
 *
 */
public class ServerGlobalState {

	private static Logger logger = Logger.getLogger(ServerGlobalState.class);

	ExecutorService executor = null;

	String id = "Server" + ("" + Math.random()).substring(2);
	// FIXME: more random here

	MyWebSocketServer server;

	public TimeoutCache timeoutCache;
	
	Thread serverThread;
	
	PubSub redis;

	public ServerGlobalState(int port) {
		executor = Executors.newCachedThreadPool();
		
		// open the port
		server = new MyWebSocketServer(this);
		Runnable starter = server.new Starter(port, this);
		serverThread = new Thread(starter);
		serverThread.setName("id");
		serverThread.setDaemon(true);
		serverThread.start();

		timeoutCache = new TimeoutCache(executor, id);
		
		redis = new JedisRedisPubSubImpl("localhost", new LocalSubHandler() );
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

	/** Incoming messages from the WS server, or incoming in general, come directly through here.
	 * 
	 * @param ctx
	 * @param child
	 */
	public void executeChannelMessage(ChannelHandlerContext ctx, String message) {
		execute(new CtxWrapper(ctx, message));
	}
	
	public void stop(){
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
	 * For knowing which server we on a member of in multi-server and multi-pool
	 * simulations.
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
				Runnable r = ServerGlobalState.deserialize(message);
				r.run();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myChannelContextStateObj.set(null);

		}

	}
	
	private static final ThreadLocal<String> latestChannel = new ThreadLocal<String>();

	class LocalSubHandler implements PubSub.Handler
	{

		@Override
		public void handle(String channel, String message) {
			executor.execute(new ChannelWrapper(channel,message));
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

		//MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@Class");
	}

	public static Runnable deserialize(String s) throws JsonParseException,
			JsonMappingException, IOException {
		Runnable obj = MAPPER.readValue(s, Runnable.class);
		return obj;
	}
	
	public static String serialize( Runnable message ) throws JsonProcessingException{
		return MAPPER.writeValueAsString(message);
	}

	static public void reply( Runnable message ){
		ChannelHandlerContext ctx = myChannelContextStateObj.get();
		//System.out.println(" have ctx name = " + ctx.name());
		try {
			String sendme = ServerGlobalState.serialize(message);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			//e.printStackTrace();
			logger.error("bad message " + message, e);
		}

	}

}
