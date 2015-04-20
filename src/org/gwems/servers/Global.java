package org.gwems.servers;

import gwems.Ack;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;

import org.apache.log4j.Logger;
import org.gwems.agents.Agent;
import org.gwems.agents.SessionAgent;
import org.gwems.agents.SimpleAgent;
import org.gwems.servers.impl.JedisRedisPubSubImpl;
import org.gwems.servers.impl.JsEnginePool;
import org.gwems.servers.impl.MyRejectedExecutionHandler;
import org.gwems.servers.impl.MyWebSocketServer;
import org.gwems.util.HalfHashTwoWayMapping;
import org.gwems.util.PubSub;
import org.gwems.util.TimeoutCache;
import org.gwems.util.TwoWayMapping;
import org.messageweb.dynamo.DynamoHelper;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

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
 *         Copyright 2015 Alan Wootton see included license.
 *
 */
public class Global implements Executor {

	public static Logger logger = Logger.getLogger(Global.class);

	private ThreadPoolExecutor executor = null;

	// We will need for the server id's top be globally unique.
	// It must never have a { in it, or probably a [ or / either.
	private static int serveIdCounter = 0;
	public String id = "Svr" + serveIdCounter++;// ("" + Math.random()).substring(2);

	private MyWebSocketServer server;

	public TimeoutCache timeoutCache;

	// The thread that watches the NIO acceptor.
	private Thread serverThread;

	private PubSub thePubSub;

	ClusterState cluster;

	public DynamoHelper dynamoHelper;

	/**
	 * In order for this to not leak it must be carefully handled. The main defense is that only agents subscribe and
	 * all agents have a timeout that unsubscribes them
	 * 
	 */
	private TwoWayMapping<Agent, String> session2channel = new HalfHashTwoWayMapping<Agent, String>();

	int port;

	private JsEnginePool jsPool;

	public int sessionTtl = SessionAgentTTL;// 15 min

	public boolean isPubSub = false;

	public static Global dummyGlobal() {
		Global global = new Global(0, null);

		return global;
	}

	public Global(int port, ClusterState cluster) {

		// SecurityManager def = java.lang.System.getSecurityManager();
		// def is null - no checks at all
		// dammit. TODO: I can't make this work.
		// java.lang.System.setSecurityManager(new MySecurityManager());

		this.cluster = cluster;
		this.port = port;

		/**
		 * A dummy has no WebSocket server and no pub sub server. It's just the thread pool and a timeoutQ
		 * 
		 */
		boolean isDummyServer = port == 0 && cluster == null;

		if (!isDummyServer)
			dynamoHelper = new DynamoHelper();

		executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new MyThreadFactory());

		// open the port
		if (!isDummyServer) {
			server = new MyWebSocketServer(this);
			Runnable starter = server.new Starter(port, this);
			serverThread = new Thread(starter);
			serverThread.setName(id + ":" + port);
			serverThread.setDaemon(true);
			serverThread.start();
		}

		// start the timeout cache.
		timeoutCache = new TimeoutCache(executor, id);

		// Start the redis pub/sub thread.
		// might be null if this server is server the root of the pub/sub.
		if (!isDummyServer)
			thePubSub = cluster.pubSubFactory(new LocalSubscriberHandler(), id);

		jsPool = new JsEnginePool(this);

		// how do we wait for server to start up?
		if (!isDummyServer)
			while (server.startedChannel == false) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		if (thePubSub != null) {
			// start a timer to keep the 'dis pubsub alive
			Agent shitAgent = new SimpleAgent(JedisRedisPubSubImpl.dummyChannel, this);
			timeoutCache.put("eTADIUdpXuaVjdijWhlE", shitAgent, 12 * 60 * 1000, () -> {
				timeoutCache.setTtl("eTADIUdpXuaVjdijWhlE", 12 * 60 * 1000);
				thePubSub.publish(JedisRedisPubSubImpl.dummyChannel, "{\"@\":\"Live\"}");
			});
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

	public void execute(Runnable r) {
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

	static final int SessionAgentTTL = Util.twoMinutes;

	public SessionAgent almost_private_EnsureSessionAgent(ChannelHandlerContext ctx) {
		SessionAgent sessionAgent;
		AttributeKey<String> key = AttributeKey.<String> valueOf("session");
		Attribute<String> sessionStringAttribute = ctx.attr(key);
		if (sessionStringAttribute.get() == null) {
			sessionAgent = new SessionAgent(this, getRandom(), ctx);
			sessionStringAttribute.set(sessionAgent.getKey());
			timeoutCache.put(sessionAgent.getKey(),
					sessionAgent,
					sessionTtl,
					() -> {
						unsubscribeAgent(sessionAgent);// Super important. We don't want the subscriptions to leak.
					// do we close the socket?? ctx.close() here kills this server
					logger.info("closed SessionAgent ip=" + sessionAgent.ipAddress + " start=" + sessionAgent.getStartTime() + " end=" + new Date().getTime()
							+ " key=" + sessionAgent.getKey());
					sessionStringAttribute.set(null);
					Channel ch = ctx.channel();
					if (ch != null)
						ch.writeAndFlush(new CloseWebSocketFrame());
				});
			// send an ack now.
			Ack ack = new Ack();
			ack.server = this.id;
			ack.session = sessionAgent.getKey();
			ack.version = "0.1";// ?? TODO: ??
			sessionAgent.writeAndFlush(ack);
			if (logger.isTraceEnabled()) {
				logger.trace("sent Ack to " + ack.session);
			}

		} else {
			sessionAgent = (SessionAgent) this.timeoutCache.get(sessionStringAttribute.get());
			if (sessionAgent.ipAddress == null && ctx.channel() != null && ctx.channel().remoteAddress() != null) {
				sessionAgent.ipAddress = ctx.channel().remoteAddress().toString();
				logger.info("new SessionAgent ip=" + sessionAgent.ipAddress + " start=" + sessionAgent.getStartTime());
			}
		}
		return sessionAgent;
	}

	/**
	 * Incoming messages from the WS channel come directly through here.
	 * 
	 * @param ctx
	 * @param child
	 */
	public void executeChannelMessage(ChannelHandlerContext ctx, String message) {
		SessionAgent sessionAgent;
		sessionAgent = almost_private_EnsureSessionAgent(ctx);
		assert ctx != null;
		if (logger.isTraceEnabled()) {
			logger.trace("Sending message to execute on " + sessionAgent + " with " + message);
		}
		sessionAgent.byteCount.addAndGet(message.length());
		sessionAgent.socketMessageQ.run(new CtxWrapper(message));
	}

	public void stop() {

		if (thePubSub != null)
			thePubSub.stop();
		if (server != null)
			server.stop();
		executor.setRejectedExecutionHandler(new MyRejectedExecutionHandler());
		try {
			executor.awaitTermination(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		executor.shutdown();
	}

	/**
	 * For sending reply messages on WS sockets which, btw, is never allowed.
	 * 
	 * @return
	 */
	// public static Optional<ChannelHandlerContext> getCtx() {
	// return context.get().ctx;
	// }

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

		String message;

		public CtxWrapper(String message) {
			super();
			this.message = message;
		}

		@Override
		public void run() {
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
			}
		}
	}

	// private static final ThreadLocal<String> latestChannel = new ThreadLocal<String>();

	/**
	 * Channels subscribed to come in through here when they come from another server through redis. Local publish
	 * messages short circuit redis.
	 * 
	 * @author awootton
	 *
	 */
	private class LocalSubscriberHandler implements PubSub.Handler {

		@Override
		public void handle(String channel, String str) {
			if (logger.isTraceEnabled()) {
				logger.trace("something from redis " + str + " on channel " + channel);
			}
			// reject the bytes before the '{'
			// TODO: there's a fancy, optimized, faster, way to do this job.
			// FIXME: write it. Also, don't slow down this thread.
			int pos = str.indexOf('{');
			if (pos < 0) {
				logger.error("bad message - missing {" + str);
				return;
			}
			String prefix = str.substring(0, pos);
			if (prefix.equals(id)) {// was sent by us
				return;// so, we don't need to broadcast again.
			}
			String message = str.substring(pos);
			// don't deserialize in the PubSub thread.
			executor.execute(() -> {
				try {
					Runnable runme = Global.deserialize(message);
					// get all the local subscribers
					Set<Agent> agents = session2channel.thing2items_get(channel);
					if (logger.isTraceEnabled()) {
						logger.trace("sending2 to " + agents + " on channel " + channel);
					}
					for (Agent agent : agents) {
						// give them the message
						agent.messageQ.run(new ChannelSubscriberWrapper(channel, agent, runme));
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
	 * above) and also in the two publish methods. x
	 * 
	 * @author awootton
	 *
	 */
	@JsonIgnoreType
	// never serialize this
	private static class ChannelSubscriberWrapper implements Runnable {

		String channel;
		Agent agent;
		Runnable runme;

		public ChannelSubscriberWrapper(String channel, Agent agent, Runnable runme) {
			super();
			this.channel = channel;
			this.agent = agent;
			this.runme = runme;
		}

		@Override
		public void run() {

			ExecutionContext ec = context.get();
			ec.subscribedChannel = Optional.of(channel);
			ec.agent = Optional.of(agent);
			runme.run();
			ec.subscribedChannel = Optional.empty();
			ec.agent = Optional.empty();
		}
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@");
	}

	// public static class XXXCustomTypeResolverBuilder extends DefaultTypeResolverBuilder
	// {
	// public XXXCustomTypeResolverBuilder()
	// {
	// super(DefaultTyping.NON_FINAL);
	// }
	//
	// @Override
	// public boolean useForType(JavaType t)
	// {
	// System.out.println(t);
	// if ( t.getRawClass() == ArrayList.class )
	// return false;
	// if ( t.getRawClass() == L.class )
	// return false;
	//
	// return false;
	// // if (t.getRawClass().getName().startsWith("test.jackson")) {
	// // return true;
	// // }
	// //
	// // return false;
	// }
	// }

	// DELETE ME
	static class XXTreeMapCustomSerializer extends JsonSerializer<TreeMap<String, ?>> {

		@Override
		public void serialize(TreeMap<String, ?> tree, JsonGenerator jgen, SerializerProvider arg2) throws IOException, JsonProcessingException {
			jgen.writeStartObject();
			Iterator<?> it = tree.entrySet().iterator();
			while (it.hasNext()) {
				@SuppressWarnings("unchecked")
				Entry<String, ?> entry = (Entry<String, ?>) it.next();
				jgen.writeString(entry.getKey());
				jgen.writeObject(entry.getValue());
			}
			jgen.writeEndObject();
		}

		@Override
		public void serializeWithType(TreeMap<String, ?> tree, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException,
				JsonProcessingException {
			// typeSer.writeTypePrefixForObject(value, gen);
			serialize(tree, gen, provider); // call your customized serialize method
			// typeSer.writeTypeSuffixForObject(value, gen);
		}
	}

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

	public void publish(String channel, Runnable runme) {
		publish(channel, runme, true, null);
	}

	public void publishLog(String channel, Runnable runme) {
		publish(channel, runme, false, null);
	}

	public void publish(String channel, Runnable runme, boolean log, Agent except) {
		if (logger.isTraceEnabled() && log) {
			logger.trace("publish sending runme " + runme + " on channel " + channel);
		}
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
		// tell the local subscribers

		executor.execute(() -> {
			ExecutionContext ec = context.get();
			ec.subscribedChannel = Optional.of(channel);
			// get all the local subscribers
			Set<Agent> agents = session2channel.thing2items_get(channel);
			if (logger.isTraceEnabled() && log) {
				logger.trace("sending on to " + agents + " on channel " + channel);
			}
			for (Agent agent : agents) {
				// give them the message
				if (agent != except)
					agent.messageQ.run(new ChannelSubscriberWrapper(channel, agent, runme));
			}
			ec.subscribedChannel = Optional.empty();
		});
		// Tack our name on the front so we don't re-broadcast when it arrives.
		// FIXME: don't need this when redis is gone
		if (thePubSub != null)
			thePubSub.publish(channel, id + str);
	}

	public void subscribe(Agent agent, String channel) {
		if (channel == null)
			return;
		if (channel.length() == 0)
			return;

		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty() && thePubSub != null) {
			thePubSub.subcribe(channel);
		}
		session2channel.add(agent, channel);
	}

	public void unsubscribe(Agent agent, String channel) {
		session2channel.remove(agent, channel);
		Set<Agent> agents = session2channel.thing2items_get(channel);
		if (agents.isEmpty() && thePubSub != null) {
			thePubSub.unsubcribe(channel);
		}
	}

	public void unsubscribeAgent(Agent agent) {
		Set<String> channels = session2channel.item2things_get(agent);
		session2channel.removeItem(agent);
		for (String channel : channels) {
			Set<Agent> agents = session2channel.thing2items_get(channel);
			if (agents.isEmpty() && thePubSub != null) {
				thePubSub.unsubcribe(channel);
			}
		}
	}

	// For sessions, and such, 128 bits will be enough.
	public static String getRandom() {
		ExecutionContext ec = context.get();
		MessageDigest md = ec.md5;
		// FIXME: more random.
		md.update(("" + Math.random()).getBytes());
		md.update(ec.lastRandom);
		byte[] bytes = md.digest();
		ec.lastRandom = bytes;
		String str = Base64.getEncoder().encodeToString(bytes);
		str = str.replaceAll("=", "");// just the random part please
		return str;
	}

	public ScriptEngine getEngine() {
		return jsPool.get();
	}

	public void returnEngine(ScriptEngine engine) {
		if (engine != null)
			jsPool.giveBack(engine);
	}

	public JsEnginePool getJsEnginePool() {
		return jsPool;
	}

}
