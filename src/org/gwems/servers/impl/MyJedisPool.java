package org.gwems.servers.impl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MyJedisPool extends JedisPool {

	public MyJedisPool(JedisPoolConfig jedisPoolConfig, String hostName, int port) {
		super(jedisPoolConfig,hostName,port);
	}
	
	/** Why am I creating this class and why do I have to do it this way?
	 * 
	 */
	public void returnJedis( Jedis jedis ){
		returnResource(jedis);
	}

}
