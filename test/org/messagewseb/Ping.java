package org.messagewseb;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.ClusterState;
import org.messageweb.Global;
import org.messageweb.WsClientImpl;
import org.messageweb.impl.MyWebSocketClientHandler;
import org.messageweb.messages.PingEcho;
import org.messageweb.util.TimeoutCache;

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
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);

		Global global = new Global(8081, new ClusterState());// starts a ws server
		WsClientImpl client = new WsClientImpl(8081);// start a client

		PingEcho p = new PingEcho();
		String key = "iodikehnehdfukff";
		p.setKey(key);
		
		WsClientImpl.cache.put(key, new AtomicInteger(0), 1000, new Runnable(){
			@Override
			public void run() { // this will happen in 1000 ms if there's no reply
				logger.error("Failing now");
				Assert.assertFalse(true);
			}
		});
		client.enqueueRunnable(p);// send the message
		logger.trace("ping message sent");
		
		// wait for message to arrive
		while (WsClientImpl.cache.get(key).toString().equals("0") ){
			try {
				//logger.trace("cache has " + WsClientTest.cache.get(key));
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		WsClientImpl.cache.remove(key);// we're done
		logger.trace("removed key now");
		
		// how do I do this?? 
		global.stop();
		client.stop();// aka client.running = false;

	}

	public static void main(String[] args) {
		Ping test = new Ping();
		test.gobabygo();

		System.out.println(" 888  888  888  888  888  888  888  888  888  main finished   main finished   main finished   main finished   main finished ");
	}

}
