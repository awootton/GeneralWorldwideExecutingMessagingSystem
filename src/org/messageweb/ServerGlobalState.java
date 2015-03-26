package org.messageweb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.messageweb.socketimpl.MyWebSocketServer;
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
	public void executeChannelMessage(ChannelHandlerContext ctx, Runnable child) {
		execute(new CtxWrapper(ctx, child));
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
		Runnable child;

		public CtxWrapper(ChannelHandlerContext ctx, Runnable child) {
			super();
			this.ctx = ctx;
			this.child = child;
		}

		@Override
		public void run() {
			myChannelContextStateObj.set(ctx);
			child.run();
			myChannelContextStateObj.set(null);

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

	/** FOr debugging - open a socket and send the message to the server
	 * 
	 * @param message
	 * @throws JsonProcessingException
	 */
//	public void injectMessage( Runnable message ) throws JsonProcessingException{
//		// we need a WS client !! 
//		String s = serialize(message);
//		
//		
//	}
	
	
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
