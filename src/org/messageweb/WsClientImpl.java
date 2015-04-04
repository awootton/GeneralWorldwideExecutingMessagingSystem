package org.messageweb;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.messageweb.impl.MyRejectedExecutionHandler;
import org.messageweb.impl.MyWebSocketClientHandler;
import org.messageweb.messages.PingEcho;
import org.messageweb.util.TimeoutCache;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This is an example of a WebSocket client. Messages the be sent are placed in the commands4serverQ Q and picked up by
 * the main thread later.
 * 
 * (atw) Messages incoming are passed to executeChannelMessage here and are usually just executed immediately.
 * <p>
 * In order to run this example you need a compatible WebSocket server. Therefore you can either start the WebSocket
 * server from the examples by running {@link io.netty.example.http.websocketx.server.WebSocketServer} or connect to an
 * existing WebSocket server such as <a href="http://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument. You don't have to specify any
 * arguments if you want to connect to the example WebSocket server, as this is the default.
 */
public final class WsClientImpl {

	public static Logger logger = Logger.getLogger(WsClientImpl.class);

	static final String URL = System.getProperty("url", "ws://127.0.0.1:8081");// + // "/websocket"

	MyWebSocketClientHandler handler;

	public boolean running = true;// just this server
	// System.setProperty("catalina.base", ".."); // so logger won't npe

	private ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool(); 

	public   TimeoutCache cache; 

	private int port;
	private String host;
	private SslContext sslCtx = null;

	private BlockingQueue<String> commands4serverQ = new ArrayBlockingQueue<String>(16);

	private static int clientInstanceCounter = 0;

	public WsClientImpl(int port) {
		super();
		this.port = port;
		
		cache = new TimeoutCache(executor, "AllClients");

		Thread thread = new Thread(() -> {
			try {
				startUp(port);
			} catch (Exception e) {
				logger.error(e);
			}
		});
		thread.setName("Client#" + clientInstanceCounter++);
		thread.setDaemon(true);
		thread.start();
		logger.info("Web CLient started port= " + port);
	}

	// class ClientStarter implements Runnable {
	//
	// int port;
	//
	// public ClientStarter(int port) {
	//
	// this.port = port;
	// }
	//
	// public void run() {
	// WsClientImpl server = WsClientImpl.this;
	// try {
	// server.startUp(port);
	// } catch (Exception e) {
	// logger.error(e);
	// }
	// }
	// }

	// hangs the thread lower down.
	private void startUp(int theport) throws Exception {
		this.port = theport;
		URI uri = new URI(URL);
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		// final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		// final int port;
		// if (uri.getPort() == -1) {
		// if ("http".equalsIgnoreCase(scheme)) {
		// port = 80;
		// } else if ("https".equalsIgnoreCase(scheme)) {
		// port = 443;
		// } else {
		// port = -1;
		// }
		// } else {
		// port = uri.getPort();
		// }

		if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
			System.err.println("Only WS(S) is supported.");
			return;
		}

		final boolean ssl = "wss".equalsIgnoreCase(scheme);
		if (ssl) {
			sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
		} else {
			sslCtx = null;
		}

		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
			// or V00.
			// If you change it to V00, ping is not supported and remember to
			// change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
			// pipeline.
			WebSocketVersion version = WebSocketVersion.V13;
			// version = WebSocketVersion.V08;
			boolean allowExtensions = false;

			handler = new MyWebSocketClientHandler(
					WebSocketClientHandshakerFactory.newHandshaker(uri, version, null, allowExtensions, new DefaultHttpHeaders()), this);

			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new MyChannelInitializer());

			ChannelFuture f = b.connect(uri.getHost(), port);
			f = f.sync();
			Channel ch = f.channel();
			handler.handshakeFuture().sync();

			// BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (running) {
				// String msg = console.readLine();
				String msg;
				try {
					msg = commands4serverQ.poll(1, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					msg = null;
				}
				if (msg == null) {
					continue;
				}
				if (logger.isTraceEnabled())
					logger.trace("pulled string from Q =" + msg);

				if ("bye".equals(msg.toLowerCase())) {
					ch.writeAndFlush(new CloseWebSocketFrame());
					ch.closeFuture().sync();
					break;
				} else if ("ping".equals(msg.toLowerCase())) {
					WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
					ch.writeAndFlush(frame);
				} else {
					WebSocketFrame frame = new TextWebSocketFrame(msg);
					ch.writeAndFlush(frame);
				}
			}
		} finally {
			group.shutdownGracefully();
		}
	}

	int getPort() {
		return port;
	}

	/**
	 * There is an Http header thing added here that I don't understand. (atw)
	 * 
	 * @author awootton
	 *
	 */
	private class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) {
			ChannelPipeline p = ch.pipeline();
			if (sslCtx != null) {
				p.addLast(sslCtx.newHandler(ch.alloc(), host, getPort()));
			}
			p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
		}

	}

	private static final ThreadLocal<ChannelHandlerContext> myClientContext = new ThreadLocal<ChannelHandlerContext>();
	private static final ThreadLocal<WsClientImpl> myClient = new ThreadLocal<WsClientImpl>();
	
	public static WsClientImpl getClient(){ // if any
		return myClient.get();
	}

	static public void Xreply(Runnable message) {
		ChannelHandlerContext ctx = myClientContext.get();
		// logger.info(" have ctx name = " + ctx.name());
		try {
			String sendme = Global.serialize(message);
			ctx.channel().write(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			// e.printStackTrace();
			logger.error("bad message " + message, e);
		}

	}

	/**
	 * Incoming messages to this client, or incoming in general, come directly through here.
	 * 
	 * try typing this into command prompt: {"@Class":"org.messageweb.messages.PingEcho","key":"someRandomKeyToDoTricksWith"}
	 * 
	 * @param ctx
	 * @param child
	 */
	public void executeChannelMessage(ChannelHandlerContext ctx, String message) {

		// executor.execute(new ClientCtxWrapper(ctx, message));
		executor.execute(() -> {
			myClientContext.set(ctx);
			myClient.set(WsClientImpl.this);
			Runnable child;
			try {
				child = Global.deserialize(message);
				child.run();
			} catch (JsonParseException e) {
				logger.error("bad message:"+message, e);
			} catch (JsonMappingException e) {
				logger.error("bad message:"+message, e);
			} catch (IOException e) {
				logger.error("bad message:"+message, e);
			}
			myClientContext.set(null);
			myClient.set(null);

		});
	}

//	private class ClientCtxWrapper implements Runnable {
//
//		ChannelHandlerContext ctx;
//		String message;
//
//		public ClientCtxWrapper(ChannelHandlerContext ctx, String message) {
//			super();
//			this.ctx = ctx;
//			this.message = message;
//		}
//
//		@Override
//		public void run() {
//			myClientContext.set(ctx);
//			myClient.set(WsClientImpl.this);
//			Runnable child;
//			try {
//				child = Global.deserialize(message);
//				child.run();
//			} catch (JsonParseException e) {
//				logger.error("bad message", e);
//			} catch (JsonMappingException e) {
//				logger.error("bad message", e);
//			} catch (IOException e) {
//				logger.error("bad message", e);
//			}
//			myClientContext.set(null);
//			myClient.set(null);
//		}
//	}

	/**
	 * Kills everything - all FIXME: should kill all the clients. Not just this one.
	 */
	public void stop() {
		executor.setRejectedExecutionHandler(new MyRejectedExecutionHandler());
		running = false;
		try {
			executor.awaitTermination(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		executor.shutdown();
	}

	/**
	 * Only wait 100 ms for q ?
	 * 
	 * @param r
	 */

	public void enqueueRunnable(Runnable r) {
		String s;
		try {
			s = Global.serialize(r);
			enqueueString(s);
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
	}

	/**
	 * DOnt do this in prod
	 * 
	 * @param r
	 */

	public void enqueueString(String s) {
		try {
			commands4serverQ.offer(s, 100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}

	/**
	 * When you run this is tries to connect to localhost on 8081 and then you can try various commands in the console.
	 * Only correct serializations of Runnables actually do anything on a Global server.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);

		logger.info("Starting main");

		WsClientImpl test = new WsClientImpl(8081);

		test.enqueueRunnable(new PingEcho());

		test.enqueueRunnable(new PingEcho());

		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String msg = console.readLine();
			if (msg == null) {
				break;
			}
			if (msg.length() <= 1) {
				continue;
			}
			if ("bye".equals(msg)) {
				test.stop();
			}
			test.enqueueString(msg);
		}
		logger.warn("777777777  main finishing 777777777  main finishing 777777777  main finishing 777777777  main finishing ");
	}
}