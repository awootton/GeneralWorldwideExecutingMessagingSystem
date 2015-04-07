package org.gwems;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.servers.impl.MyWebSocketClientHandler;
import org.gwems.util.TimeoutCache;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.testmessages.PingEcho;

/**
 * Try to open up sockets, send a ping, close it all up.
 * 
 * @author awootton
 *
 */
public class Ping {

	public static Logger logger = Logger.getLogger(Ping.class);

	@Test
	public void gobabygo() {
		
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);

		Global global = new Global(9981, new ClusterState());// starts a ws server
		WsClientImpl client = new WsClientImpl(9981);// start a client

		PingEcho p = new PingEcho();
		String key = "iodikehnehdfukff";
		p.setKey(key);

		client.cache.put(key, new AtomicInteger(0), 1000, () -> { // this will happen in 1000 ms 
					logger.error("Failing now");
					Assert.assertFalse(true);
				});
		client.enqueueRunnable(p);// send the message
		logger.info("ping message sent");

		// wait for message to arrive
		while (client.cache.get(key).toString().equals("0")) {
			try {
				// logger.trace("cache has " + WsClientTest.cache.get(key));
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		client.cache.remove(key);// we're done
		logger.trace("removed key now. Ping test is complete");

		// how do I do this??
		global.stop();
		client.stop();

	}

	public static void main(String[] args) {

		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		
		MyRedisPubSub.logger.setLevel(Level.TRACE);

		Ping test = new Ping();
		test.gobabygo();

		System.out.println(" 888  888  888  888  888  888  888  888  888  main finished   main finished   main finished   main finished   main finished ");
	}

}
