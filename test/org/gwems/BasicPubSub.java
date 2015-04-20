package org.gwems;

import gwems.Js;
import gwems.Publish;
import gwems.Push2Client;
import gwems.Subscribe;

import java.io.IOException;

import org.apache.log4j.Level;
import org.gwems.agents.SessionAgent;
import org.gwems.agents.SimpleAgent;
import org.gwems.common.core.StartServers;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.JedisRedisPubSubImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.servers.impl.MyWebSocketClientHandler;
import org.gwems.servers.impl.MyWebSocketServer;
import org.gwems.servers.impl.MyWebSocketServerHandler;
import org.gwems.util.Stopwatch;
import org.gwems.util.TimeoutCache;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.experiments.AgentFinder;
import org.messageweb.testmessages.AgentEcho;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

public class BasicPubSub extends StartServers {

	/** Your basic one client to another client on same server.
	 * 
	 */
	@Test
	public void pubsub() {

		WsClientImpl client0 = clients.get(0);

		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client0.userMap.get("sessionId") == null));

		WsClientImpl client2 = new WsClientImpl("localhost", 8081);// start a client

		client2.enqueueRunnable(new Subscribe("aChannel2Sub2"));
		// wait for the ack
		boolean ok = Stopwatch.tryForLessThan(1, () -> client2.userMap.get("sessionId") == null);
		Assert.assertTrue(ok);

		Js jsmessage = new Js();
		jsmessage.js = "var received = 12345678";// our test message for client2
		client0.enqueueRunnable(new Publish("aChannel2Sub2", new Push2Client(jsmessage)));

		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client2.bindings == null));
		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client2.bindings.get("received") == null));

		Assert.assertEquals(client2.bindings.get("received"), new Integer(12345678));

		client2.stop();
	}
	
	@Test
	public void pubsubCross() {

		WsClientImpl client0 = clients.get(0);
		WsClientImpl client1 = clients.get(1);

		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client0.userMap.get("sessionId") == null));

		client1.enqueueRunnable(new Subscribe("aChannel2Sub2"));
		// wait for the ack
		boolean ok = Stopwatch.tryForLessThan(1, () -> client1.userMap.get("sessionId") == null);
		Assert.assertTrue(ok);

		Js jsmessage = new Js();
		jsmessage.js = "var received = 12345678";// our test message for client2
		client0.enqueueRunnable(new Publish("aChannel2Sub2", new Push2Client(jsmessage)));

		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client1.bindings == null));
		Assert.assertTrue(Stopwatch.tryForLessThan(1, () -> client1.bindings.get("received") == null));

		Assert.assertEquals(client1.bindings.get("received"), new Integer(12345678));
		client1.bindings = null;// reset 
	}


	public static void main(String[] args) {

		Global.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		PingTest.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		SessionAgent.logger.setLevel(Level.TRACE);
		LogonMessage.logger.setLevel(Level.TRACE);
		SimpleAgent.logger.setLevel(Level.TRACE);
		AgentEcho.logger.setLevel(Level.TRACE);

		AgentFinder.logger.setLevel(Level.TRACE);

		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);

		BasicPubSub test = new BasicPubSub();

		setup();

		test.pubsubCross();

//		try {
//			Thread.sleep(5 * 1000);
//		} catch (InterruptedException e) {
//		}
		
		closeAll();
	}

}
