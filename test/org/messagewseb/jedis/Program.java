package org.messagewseb.jedis;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

public class Program {

	public static final String CHANNEL_NAME = "commonChannelTestAtwChannel";

	private static Logger logger = Logger.getLogger(Program.class);

	public static void main(String[] args) throws Exception {

	//	final JedisPoolConfig poolConfig = new JedisPoolConfig();
		
		// org.apache.commons.pool2   PooledObjectFactory is missing but it still runs

	//	final JedisPool jedisPool = new JedisPool("localhost");// new JedisPool(gencon, "localhost", 6379, 0);
		final Jedis subscriberJedis = new Jedis("localhost");//jedisPool.getResource();
		final Subscriber subscriber = new Subscriber();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
					subscriberJedis.subscribe(subscriber, CHANNEL_NAME);
					logger.info("Subscription ended.");
				} catch (Exception e) {
					logger.error("Subscribing failed.", e);
				}
			}
		}).start();

		final Jedis publisherJedis = new Jedis("localhost");//jedisPool.getResource();

		new Publisher(publisherJedis, CHANNEL_NAME,  subscriberJedis, subscriber).start();

		subscriber.unsubscribe();
//		jedisPool.returnResource(subscriberJedis);
//		jedisPool.returnResource(publisherJedis);
	}

}