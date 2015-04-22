package org.gwems.servers.impl;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.gwems.util.PubSub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

public class JedisRedisPubSubImpl extends PubSub {

	public static Logger logger = Logger.getLogger(JedisRedisPubSubImpl.class);

	MyRedisPubSub myPubSub;

	Jedis subscribingRedis;

	String hostName;

	int port;

	private MyJedisPool pool;

	public JedisRedisPubSubImpl(String hostName, int port, Handler handler, String globalName) {
		this.hostName = hostName;
		this.port = port;
		myPubSub = new MyRedisPubSub();
		myPubSub.handler = handler;

		// not from pool - permanent.
		subscribingRedis = new Jedis(hostName, port);

		pool = new MyJedisPool(new JedisPoolConfig(), hostName, port);

		Thread thread = new Thread(new RunPS());
		thread.setName("Redis_Sub_Runner" + globalName);
		thread.setDaemon(true);
		thread.start();
		while (!myPubSub.isSubscribed()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	class RunPS implements Runnable {
		public void run() {
			logger.info("Redis subscriber thread started watching port " + port);
			logger.trace("MyRedisPubSub starting proceed");
			subscribingRedis.subscribe(myPubSub, dummyChannel);
			logger.info("MyRedisPubSub thread finished and quitting");
			subscribingRedis.close();
		}
	}

	@Override
	public void publish(String channel, Runnable message) {
		Jedis jedis = pool.getResource();
		try {
			jedis.publish(channel, Global.serialize(message));
		} catch (Exception e) {
			logger.error("what?", e);
		} finally {
			pool.returnJedis(jedis);
		}
	}

	@Override
	public void subcribe(String... channels) {
		if (logger.isTraceEnabled()) {
			logger.trace("sending subscribe to redis:" + Arrays.toString(channels));
		}
		myPubSub.subscribe(channels);
	}

	@Override
	public void unsubcribe(String... channels) {
		if (logger.isTraceEnabled()) {
			logger.trace("sending unsubcribe to redis:" + Arrays.toString(channels));
		}
		myPubSub.unsubscribe(channels);
	}

	@Override
	public void setHandler(Handler handler) {
		myPubSub.handler = handler;
	}

	@Override
	public void stop() {
		myPubSub.unsubscribe(dummyChannel);
		myPubSub.unsubscribe();
	}

}
