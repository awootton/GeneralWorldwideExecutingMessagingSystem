package org.gwems.servers.impl;

import org.apache.log4j.Logger;
import org.gwems.util.PubSub.Handler;

import redis.clients.jedis.JedisPubSub;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MyRedisPubSub extends JedisPubSub {

	public static Logger logger = Logger.getLogger(MyRedisPubSub.class);

	Handler handler = null;

	@Override
	public void onMessage(String channel, String message) {
		if (logger.isTraceEnabled())
			logger.trace("onMessage " + channel + "  =" + message);
		handler.handle(channel, message);
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		logger.error("we are not using this feature");
		throw new NotImplementedException();
	}

	/** These are the REPLIES from when we call sub and unsub in 
	 * 
	 */
	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		if (logger.isTraceEnabled())
			logger.trace("subscribed to " + channel + " count=" + subscribedChannels);
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		if (logger.isTraceEnabled())
			logger.trace("onUnsubscribe to " + channel + " count=" + subscribedChannels);
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.error("we are not using this feature");
		throw new NotImplementedException();
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.error("we are not using this feature");
		throw new NotImplementedException();
	}

}
