package org.gwems;

import gwems.Js;
import gwems.Publish;
import gwems.Push2Client;
import gwems.Subscribe;

import org.apache.log4j.Level;
import org.gwems.agents.SessionAgent;
import org.gwems.agents.SimpleAgent;
import org.gwems.common.core.StartServers;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.JedisRedisPubSubImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.servers.impl.MyWebSocketServer;
import org.gwems.util.Stopwatch;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

import d.P2C;
import d.Pub;
import d.Sub;

public class BasicPubSub extends StartServers {

	/**
	 * Your basic one client to another client on same server.
	 * 
	 */
	@Test
	public void pubsub() {

		WsClientImpl client0 = clients.get(0);

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client0.userMap.get("sessionId") == null));

		WsClientImpl client2 = new WsClientImpl("localhost", 8081);// start a client

		client2.enqueueRunnable(new Subscribe("aChannel2Sub2"));
		// wait for the ack
		boolean ok = Stopwatch.tryAwhile(1, () -> client2.userMap.get("sessionId") == null);
		Assert.assertTrue(ok);

		Js jsmessage = new Js();
		jsmessage.js = "var received = 12345678";// our test message for client2
		client0.enqueueRunnable(new Publish("aChannel2Sub2", new Push2Client(jsmessage)));

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client2.bindings == null));
		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client2.bindings.get("received") == null));

		Assert.assertEquals(client2.bindings.get("received"), new Integer(12345678));

		client2.stop();
	}

	int counter = 0;

	@Test
	public void pubsubCross() {

		WsClientImpl client0 = clients.get(0);
		WsClientImpl client1 = clients.get(1);

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client0.userMap.get("sessionId") == null));

		counter = 0;//
		Stopwatch.tryAwhile(1, () -> {
			System.out.println();
			return counter++ < 5;
		});

		global0.subscriptionsRecorded = 0;
		global1.subscriptionsRecorded = 0;
		global2.subscriptionsRecorded = 0;
		global3.subscriptionsRecorded = 0;
		gwemdisroot.subscriptionsRecorded = 0;
		gwemdis0.subscriptionsRecorded = 0;
		gwemdis1.subscriptionsRecorded = 0;
		client1.enqueueRunnable(new Subscribe("aChannel2Sub2xxx"));
		// wait for the ack
		boolean ok = Stopwatch.tryAwhile(1, () -> client1.userMap.get("sessionId") == null);
		Assert.assertTrue(ok);

		counter = 0;//
		Stopwatch.tryAwhile(1, () -> {
			System.out.println();
			return counter++ < 5;
		});
		Assert.assertEquals(global0.subscriptionsRecorded,0);
		Assert.assertEquals(global1.subscriptionsRecorded,1);
		Assert.assertEquals(gwemdis0.subscriptionsRecorded,1);
		Assert.assertEquals(gwemdis1.subscriptionsRecorded,0);
		Assert.assertEquals(gwemdisroot.subscriptionsRecorded,1);
		// so now we should be correctly subscribed.
		// let's add a sub in client2 who subs from 6380 and from 6381
		clients.get(2).enqueueRunnable(new Subscribe("aChannel2Sub2xxx"));
		counter = 0;//
		Stopwatch.tryAwhile(1, () -> {
			System.out.println();
			return counter++ < 5;
		});
		Assert.assertEquals(global0.subscriptionsRecorded,0);
		Assert.assertEquals(global1.subscriptionsRecorded,1);
		Assert.assertEquals(global2.subscriptionsRecorded,1);
		Assert.assertEquals(global3.subscriptionsRecorded,0);
		Assert.assertEquals(gwemdis0.subscriptionsRecorded,1);
		Assert.assertEquals(gwemdis1.subscriptionsRecorded,1);
		Assert.assertEquals(gwemdisroot.subscriptionsRecorded,2);
		
		Js jsmessage = new Js();
		jsmessage.js = "var received = 12345678";// our test message for client2
		client0.enqueueRunnable(new Publish("aChannel2Sub2xxx", new Push2Client(jsmessage)));

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client1.bindings == null));
		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client1.bindings.get("received") == null));

		Assert.assertEquals(client1.bindings.get("received"), new Integer(12345678));
		client1.bindings = null;// reset
	}
	
	@Test
	public void pubsubCrossCross() {

		WsClientImpl client0 = clients.get(0);
		WsClientImpl client3 = clients.get(3);

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client0.userMap.get("sessionId") == null));

	 	client3.enqueueRunnable(new Subscribe("aChannel2Sub2xyy"));
		// wait for the ack
		boolean ok = Stopwatch.tryAwhile(1, () -> client3.userMap.get("sessionId") == null);
		Assert.assertTrue(ok);
		
		Js jsmessage = new Js();
		jsmessage.js = "var received = 9876;";// our test message for client2
		client0.enqueueRunnable(new Publish("aChannel2Sub2xyy", new Push2Client(jsmessage)));

		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client3.bindings == null));
		Assert.assertTrue(Stopwatch.tryAwhile(1, () -> client3.bindings.get("received") == null));

		Assert.assertEquals(client3.bindings.get("received"), new Integer(9876));
		client3.bindings = null;// reset
	}


	public static void main(String[] args) {

		Global.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		// MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		PingTest.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		// TimeoutCache.logger.setLevel(Level.TRACE);
		// MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		SessionAgent.logger.setLevel(Level.TRACE);
		LogonMessage.logger.setLevel(Level.TRACE);
		SimpleAgent.logger.setLevel(Level.TRACE);
		AgentEcho.logger.setLevel(Level.TRACE);

		AgentFinder.logger.setLevel(Level.TRACE);

		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);

		Pub.logger.setLevel(Level.TRACE);
		Sub.logger.setLevel(Level.TRACE);
		P2C.logger.setLevel(Level.TRACE);

		BasicPubSub test = new BasicPubSub();

		setup();

		test.pubsubCrossCross();

		closeAll();
	}

}
