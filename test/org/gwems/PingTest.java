package org.gwems;

import gwems.Js;
import gwems.Publish;
import gwems.Push2Client;
import gwems.Subscribe;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.servers.impl.MyWebSocketClientHandler;
import org.gwems.servers.impl.MyWebSocketServer;
import org.gwems.servers.impl.WsLoggingHandler;
import org.gwems.util.Stopwatch;
import org.gwems.util.TimeoutCache;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.messageweb.testmessages.PingEcho;


/**
 * Try to open up sockets, send a ping, close it all up.
 * 
 * @author awootton
 *
 */
public class PingTest {

	public static Logger logger = Logger.getLogger(PingTest.class);

	static Global global;
	static WsClientImpl client;

	@BeforeClass
	public static void setup() {

		ClusterState cluster = new ClusterState();
		cluster.redis_server = null;// is root
		global = new Global(9981, cluster);// starts a ws server
		
		client = new WsClientImpl("localhost", 9981 );// start a client

	}

	@AfterClass
	public static void closeAll() {
		// how do I do this??
		global.stop();
		client.stop();
	}

	@Test
	public void pubsub() {

		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client.userMap.get("sessionId") == null ));

		WsClientImpl client2 = new WsClientImpl("localhost", 9981);// start a client

		client2.enqueueRunnable(new Subscribe("aChannel2Sub2"));
		// wait for the ack
		boolean ok = Stopwatch.tryForLessThan(1, () -> client2.userMap.get("sessionId") == null );
		Assert.assertTrue(ok);


		Js jsmessage = new Js();
		jsmessage.js = "var received = 12345678";//our test message for client2
		client.enqueueRunnable(new Publish("aChannel2Sub2", new Push2Client(jsmessage)));


		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client2.bindings  == null ));
		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client2.bindings.get("received")  == null ));
		
		Assert.assertEquals( client2.bindings.get("received"), new Integer(12345678)); 
		
		client2.stop();
	}

	@Test
	public void gobabygo() {

		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);

		PingEcho p = new PingEcho();
		String key = "iodikehnehdfukff";
		p.setKey(key);

		client.global.timeoutCache.put(key, new AtomicInteger(0), 1000, () -> { // this will happen in 1000 ms
					logger.error("Failing now");
					Assert.assertFalse(true);
				});
		client.enqueueRunnable(p);// send the message
		logger.info("ping message sent");

		// wait for message to arrive
		while (client.global.timeoutCache.get(key).toString().equals("0")) {
			try {
				// logger.trace("cache has " + WsClientTest.cache.get(key));
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		client.global.timeoutCache.remove(key);// we're done
		logger.trace("removed key now. Ping test is complete");

	}

	public static void main(String[] args) {

		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		PingTest.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);

		MyRedisPubSub.logger.setLevel(Level.TRACE);

		Global.logger.setLevel(Level.TRACE);

		SessionAgent.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		
		WsLoggingHandler.logger.setLevel(Level.TRACE);
		

		setup();

		PingTest test = new PingTest();
		test.pubsub();
		// test.gobabygo();

		System.out.println(" 888  888  888  888  888  888  888  888  888  main finished   main finished   main finished   main finished   main finished ");

		closeAll();

	}

}
