package org.messageweb;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.messageweb.experiments.TimeoutCache;

public class ServerGlobalState {

	private static Logger logger = Logger.getLogger(ServerGlobalState.class);

	Executor executor = null;

	String id = "Server" + ("" + Math.random()).substring(2);
	// FIXME: more random here

	MyWebSocketServer server;

	public TimeoutCache cache;

	public ServerGlobalState(int port) {
		executor = Executors.newCachedThreadPool();
		// open the port
		server = new MyWebSocketServer(this);
		execute(server.new Starter(port, this));

		cache = new TimeoutCache(executor);
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

}
