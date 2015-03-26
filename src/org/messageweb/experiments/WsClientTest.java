package org.messageweb.experiments;

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
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.messageweb.ServerGlobalState;
import org.messageweb.messages.PingEcho;
import org.messageweb.util.TimeoutCache;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This is an example of a WebSocket client.
 * <p>
 * In order to run this example you need a compatible WebSocket server. Therefore you can either start the WebSocket server from the examples by running
 * {@link io.netty.example.http.websocketx.server.WebSocketServer} or connect to an existing WebSocket server such as <a
 * href="http://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument. You don't have to specify any arguments if you want to connect to the
 * example WebSocket server, as this is the default.
 */
public final class WsClientTest {

	public static Logger logger = Logger.getLogger(WsClientTest.class);

	static final String URL = System.getProperty("url", "ws://127.0.0.1:8081");// + // "/websocket"

	MyWebSocketClientHandler handler;

	public boolean running = true;// just this server
	// System.setProperty("catalina.base", ".."); // so logger won't npe

	private static ExecutorService executor = Executors.newCachedThreadPool();// all clients in test share this

	public static TimeoutCache cache;// all clients in test share this
	static {
		cache = new TimeoutCache(executor, "AllClients");
	}

	private int port;
	private String host;
	private SslContext sslCtx = null;

	private BlockingQueue<String> commands4serverQ = new ArrayBlockingQueue<String>(16);

	public WsClientTest(int port) {
		super();
		this.port = port;

		Runnable starter = new ClientStarter(port);
		Thread thread = new Thread(starter);
		thread.setName("id");
		thread.setDaemon(true);
		thread.start();
		logger.info("Web CLient started port= " + port);
	}

	class ClientStarter implements Runnable {

		int port;

		public ClientStarter(int port) {

			this.port = port;
		}

		public void run() {
			WsClientTest server = WsClientTest.this;
			try {
				server.startUp(port);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

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
	private static final ThreadLocal<WsClientTest> myClient = new ThreadLocal<WsClientTest>();

	static public void reply(Runnable message) {
		ChannelHandlerContext ctx = myClientContext.get();
		// logger.info(" have ctx name = " + ctx.name());
		try {
			String sendme = ServerGlobalState.serialize(message);
			ctx.channel().write(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			// e.printStackTrace();
			logger.error("bad message " + message, e);
		}

	}

	/**
	 * Incoming messages to this client, or incoming in general, come directly through here.
	 * 
	 * @param ctx
	 * @param child
	 */
	public void executeChannelMessage(ChannelHandlerContext ctx, Runnable child) {
		executor.execute(new ClientCtxWrapper(ctx, child));
	}

	private class ClientCtxWrapper implements Runnable {

		ChannelHandlerContext ctx;
		Runnable child;

		public ClientCtxWrapper(ChannelHandlerContext ctx, Runnable child) {
			super();
			this.ctx = ctx;
			this.child = child;
		}

		@Override
		public void run() {
			myClientContext.set(ctx);
			myClient.set(WsClientTest.this);
			child.run();
			myClientContext.set(null);
			myClient.set(null);

		}

	}

	/** Kills everything - all 
	 * FIXME: should kill all the clients. Not just this one.
	 */
	public void stop() {
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
		try {
			String s = ServerGlobalState.serialize(r);
			commands4serverQ.offer(s, 100, TimeUnit.MILLISECONDS);
		} catch (JsonProcessingException e) {
			logger.error(e);
		} catch (InterruptedException e) {
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

	public static void main(String[] args) throws Exception {

		WsClientTest.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);

		logger.info("Starting main");

		WsClientTest test = new WsClientTest(8081);

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
		logger.info("777777777  main finishing 777777777  main finishing 777777777  main finishing 777777777  main finishing ");
	}
}