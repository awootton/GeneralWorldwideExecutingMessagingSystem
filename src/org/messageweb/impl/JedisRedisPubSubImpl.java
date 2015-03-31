package org.messageweb.impl;

import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.util.PubSub;

import redis.clients.jedis.Jedis;

public class JedisRedisPubSubImpl extends PubSub {

	public static Logger logger = Logger.getLogger(JedisRedisPubSubImpl.class);

	// Jedis jedis;

	MyRedisPubSub myPubSub;

	Jedis subscribingRedis;

	String server;

	private final String dummyChannel = "AHUAp4xu9FqRobj8zwn2vBI6Anag1t8Z5z6SWjn8_neverUseThisChannel";// random.org

	public JedisRedisPubSubImpl(String server, Handler handler, String globalName) {
		this.server = server;
		// jedis = new Jedis(server);
		myPubSub = new MyRedisPubSub();
		myPubSub.handler = handler;

		subscribingRedis = new Jedis(server);

		Thread thread = new Thread(new RunPS());
		thread.setName("Redis_Sub_Runner" + globalName);
		thread.setDaemon(true);
		thread.start();
		while (!myPubSub.isSubscribed()) {
			// System.out.print (myPubSub.getSubscribedChannels() + " ");
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		System.out.println();
	}

	class RunPS implements Runnable {
		public void run() {
			logger.trace("MyRedisPubSub starting proceed");
			subscribingRedis.subscribe(myPubSub, dummyChannel);
			logger.info("MyRedisPubSub thread finished and quitting");
			subscribingRedis.close();
		}
	}

	public Jedis getJedis() {// FIXME: needs pool
		return new Jedis(server);
	}

	@Override
	public void publish(String channel, String message) {
		getJedis().publish(channel, message);
	}

	@Override
	public void subcribe(String... channels) {
		myPubSub.subscribe(channels);
	}

	@Override
	public void unsubcribe(String... channels) {
		myPubSub.unsubscribe(channels);
	}

	@Override
	public void setHandler(Handler handler) {
		myPubSub.handler = handler;
	}

	@Override	public void stop() {
		myPubSub.unsubscribe(dummyChannel);
		myPubSub.unsubscribe();
	}

}
