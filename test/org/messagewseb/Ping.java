package org.messagewseb;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.ServerGlobalState;
import org.messageweb.experiments.MyWebSocketClientHandler;
import org.messageweb.experiments.WsClientTest;
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
		
		WsClientTest.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);

		ServerGlobalState global = new ServerGlobalState(8081);// starts a ws server
		WsClientTest client = new WsClientTest(8081);// start a client

		PingEcho p = new PingEcho();
		String key = "iodikehnehdfukff";
		p.setKey(key);
		
		WsClientTest.cache.put(key, new AtomicInteger(0), 1000, new Runnable(){
			@Override
			public void run() { // this will happen in 1000 ms if there's no reply
				logger.error("Failing now");
				Assert.assertFalse(true);
			}
		});
		client.enqueueRunnable(p);// send the message
		logger.trace("ping message sent");
		
		// wait for message to arrive
		while (WsClientTest.cache.get(key).toString().equals("0") ){
			try {
				//logger.trace("cache has " + WsClientTest.cache.get(key));
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		WsClientTest.cache.remove(key);// we're done
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
