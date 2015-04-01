package org.messageweb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.messageweb.agents.Agent;
import org.messageweb.agents.SessionAgent;
import org.messageweb.dynamo.DynamoHelper;
import org.messageweb.impl.JedisRedisPubSubImpl;
import org.messageweb.impl.MyRejectedExecutionHandler;
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
 * This represents one 'server' and by that I mean a web socket (ws) port that is accepting incoming connections.
 * 
 * There is a ThreadLocal containing a reference to one of these at all times. We would just make it a static singleton
 * except that we desire to run several of these, on different ports, in a single JVM for debugging and simulation.
 * 
 * As a central object (in a situation where no singletons are allowed) it is natural for various and sundry services
 * that would be distinct from other servers to congregate here. In particular there is a thread pool, the pub/sub maps,
 * the timeout cache, the redis connection, and the nio port acceptor.
 * 
 * TODO: make an interface and an impl. Hide the helper classes. Clean it up.
 * 
 * @author awootton
 *
 */
public class Global implements Executor {

	public static Logger logger = Logger.getLogger(Global.class);

	private ThreadPoolExecutor executor = null;

	private static int serveIdCounter = 0;
	public String id = "Server" + serveIdCounter++;// ("" + Math.random()).substring(2);
	// FIXME: more random here

	private MyWebSocketServer server;

	public TimeoutCache timeoutCache;

	// The thread that watches the NIO acceptor.
	private Thread serverThread;

	private PubSub redisPubSub;

	ClusterState cluster;

	public DynamoHelper dynamoHelper;

	TwoWayMapping<Agent, String> session2channel = new TwoWayMapping<Agent, String>();

	int port;

	public Global(int port, ClusterState cluster) {

		this.cluster = cluster;
		this.port = port;

		dynamoHelper = new DynamoHelper();

		executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new MyThreadFactory());

		// open the port
		server = new MyWebSocketServer(this);
		Runnable starter = server.new Starter(port, this);
		serverThread = new Thread(starter);
		serverThread.setName(id + ":" + port);
		serverThread.setDaemon(true);
		serverThread.start();

		// start the timeout cache.
		timeoutCache = new TimeoutCache(executor, id);

		// Start the redis pub/sub thread.
		redisPubSub = new JedisRedisPubSubImpl(cluster.redis_server, cluster.redis_port, new LocalSubscriberHandler(), id);

		// how do we wait for server to start up?
		while (server.startedChannel == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ":" + id + ":" + port;
	}

	private class MyThreadFactory implements ThreadFactory {

		int count = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			String name = "Global_Worker:" + id + ":" + count++;
			thread.setName(name);
			thread.setDaemon(true);
			return thread;
		}

	}

	// private class GlobalStateSetter implements Runnable {
	// Runnable child;
	//
	// public GlobalStateSetter(Runnable child) {
	// super();
	// this.child = child;
	// }
	//
	// @Override
	// public void run() {
	// ExecutionContext ec = context.get();
	// ec.global = ServerGlobalState.this;
	// child.run();
	// ec.global = null;
	// }
	// }

	public void execute(Runnable r) {
		// executor.execute(new GlobalStateSetter(r));
		executor.execute(() -> {
			ExecutionContext ec = context.get();
			ec.global = Global.this;
			r.run();
			ec.global = null;
		});
	}

	private static final ThreadLocal<ExecutionContext> context = new ThreadLocal<ExecutionContext>() {
		@Override
		protected ExecutionContext initialValue() {
			return new ExecutionContext();
		}
	};

	static final int SessionAgentTTL = 120 * 1000;// in config??

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

		Attribute<String> sessionStringAttribute = ctx.attr(AttributeKey.<String> newInstance("session"));
		SessionAgent sessionAgent;
		if (sessionStringAttribute.get() == null) {
			sessionAgent = new SessionAgent(this, getRandom());
			sessionStringAttribute.set(sessionAgent.sub);
			timeoutCache.put(sessionAgent.sub, sessionAgent, SessionAgentTTL, () -> {
				ctx.close();
			});
		} else {
			sessionAgent = (SessionAgent) this.timeoutCache.get(sessionStringAttribute.get());
		}
		sessionAgent.messageQ.run(new CtxWrapper(ctx, message));
	}

	public void stop() {
		server.stop();
		executor.setRejectedExecutionHandler(new MyRejectedExecutionHandler());
		executor.shutdown();
	}

	/**
	 * For sending reply messages on WS sockets
	 * 
	 * @return
	 */
	public static Optional<ChannelHandlerContext> getCtx() {
		return context.get().ctx;
	}

	/**
	 * For knowing which server we on a member of in multi-server and multi-pool simulations.
	 * 
	 * @return
	 */
	public static Global getGlobal() {
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
			ec.ctx = Optional.of(ctx);
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("CtxWrapper deserialize  " + message);
				}
				Runnable r = Global.deserialize(message);
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
			executor.execute(() -> {
				try {
					Runnable runme = Global.deserialize(message);
					// get all the local subscribers
					Set<Agent> agents = session2channel.thing2items_get(channel);

					for (Agent agent : agents) {
						// give them the message
						agent.messageQ.run(new ChannelSunscriberWrapper(channel, agent, runme));
					}

				} catch (JsonProcessingException e) {
					logger.error("bad message " + message, e);
				} catch (IOException e) {
					logger.error("bad message io " + message, e);
				} finally {
				}
			});
		}
	}

	/**
	 * This was a lambda but it's used in several places here. Both in the receiver of redis (LocalSubscriberHandler
	 * above) and also in the publish.
	 * 
	 * @author awootton
	 *
	 */
	private static class ChannelSunscriberWrapper implements Runnable {

		String channel;
		Agent agent;
		Runnable runme;

		public ChannelSunscriberWrapper(String channel, Agent agent, Runnable runme) {
			super();
			this.channel = channel;
			this.agent = agent;
			this.runme = runme;
		}

		@Override
		public void run() {

			ExecutionContext ec = context.get();
			ec.subscribedChannel = Optional.of(channel);
			// System.out.println(" ######### SEtting context for " + channel);
			ec.agent = Optional.of(agent);
			runme.run();
			ec.subscribedChannel = Optional.empty();
			ec.agent = Optional.empty();

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

	/**
	 * Note that the @Class key will NOT be missing !
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public static ObjectNode serialize2node(Object message) throws IOException {
		Object obj = plain_mapper.valueToTree(message);
		ObjectNode node = (ObjectNode) obj;
		node.put("@Class", message.getClass().getName());
		return (ObjectNode) obj;
	}

	static public void reply(Runnable message) {
		ChannelHandlerContext ctx = context.get().ctx.get();
		try {
			String sendme = Global.serialize(message);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			logger.error("bad message " + message, e);
		}
	}

	public void publish(String channel, String message) {
		if (channel == null)
			return;
		if (channel.length() == 0)
			return;
		redisPubSub.publish(channel, message);
		// this is not a good idea because things will end up
		// running twice

		// tell the local subscribers
		// executor.execute(() -> {
		// ExecutionContext ec = context.get();
		// ec.subscribedChannel = Optional.of(channel);
		// try {
		// Runnable runme = Global.deserialize(message);
		// // get all the local subscribers
		// Set<Agent> agents = session2channel.thing2items_get(channel);
		// for (Agent agent : agents) {
		// // give them the message
		// agent.messageQ.run(new ChannelSunscriberWrapper(channel, agent, runme));
		// }
		//
		// } catch (JsonProcessingException e) {
		// logger.error("bad message " + message, e);
		// } catch (IOException e) {
		// logger.error("bad message io " + message, e);
		// } finally {
		// ec.subscribedChannel = Optional.empty();
		// }
		// });
	}

	public void publish(String channel, Runnable runme) {
		if (channel == null)
			return;
		if (channel.length() == 0)
			return;
		String str = "";
		try {
			str = serialize(runme);
		} catch (JsonProcessingException e) {
			logger.error("bad message io " + runme, e);
		}
		if (str.length() == 0)
			return;
		redisPubSub.publish(channel, str);
		// tell the local subscribers
		// This is a bad idea because this same thing is going
		// to happen when the redis pub arrives and it's awkward to
		// prevent that.

		// executor.execute(() -> {
		// ExecutionContext ec = context.get();
		// ec.subscribedChannel = Optional.of(channel);
		// // get all the local subscribers
		// Set<Agent> agents = session2channel.thing2items_get(channel);
		// for (Agent agent : agents) {
		// // give them the message
		// agent.messageQ.run(new ChannelSunscriberWrapper(channel, agent, runme));
		// }
		// ec.subscribedChannel = Optional.empty();
		// });
	}

	public void subscribe(Agent agent, String channel) {
		if (channel == null)
			return;
		if (channel.length() == 0)
			return;

		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty()) {
			redisPubSub.subcribe(channel);
		}
		session2channel.add(agent, channel);
	}

	public void unsubscribe(Agent agent, String channel) {
		session2channel.remove(agent, channel);
		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty()) {
			redisPubSub.unsubcribe(channel);
		}
	}

	public static String getRandom() {
		ExecutionContext ec = context.get();
		MessageDigest md = ec.sha256;
		// FIXME: more random.
		md.update(("" + Math.random()).getBytes());
		md.update(ec.lastRandom);
		byte[] bytes = md.digest();
		ec.lastRandom = bytes;
		String str = Base64.getEncoder().encodeToString(bytes);
		return str;
	}

}
