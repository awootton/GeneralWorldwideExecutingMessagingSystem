package org.messagewseb.jedis;


import org.apache.log4j.Logger;

import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {

   // private static Logger logger = LoggerFactory.getLogger(Subscriber.class);
    public static Logger logger = Logger.getLogger(Subscriber.class);

    @Override
    public void onMessage(String channel, String message) {
        logger.info("Message received. Channel:" +channel+ ", Msg: " + message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    	 logger.info("onSubscribe. Channel: " + channel + " Msg: " + channel);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }
}