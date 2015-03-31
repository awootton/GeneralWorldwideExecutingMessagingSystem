package org.messagewseb.temperature;

import java.io.IOException;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.Global;
import org.messageweb.WsClientImpl;
import org.messageweb.agents.SessionAgent;
import org.messageweb.agents.SimpleAgent;
import org.messageweb.dynamo.MyUser;
import org.messageweb.impl.MyWebSocketClientHandler;
import org.messageweb.messages.AgentEcho;
import org.messageweb.messages.LogonMessage;
import org.messageweb.messages.PingEcho;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.socketimpl.MyWebSocketServerHandler;
import org.messageweb.util.TimeoutCache;
import org.messagewseb.Ping;
import org.messagewseb.common.core.StartServers;

public class TempDataLogger extends StartServers {

	/**
	 * Sensor uploads data Saved with id, time, value
	 * 
	 * encrypted?
	 * 
	 * simplest agent model - just post to agent
	 * 
	 * @throws IOException
	 * 
	 * 
	 * 
	 */

	@Test
	public void demoOne() throws IOException {

		// ping for agent. Should fail.

		String channel = "AbcDefHijQQQQQ";

		AgentFinder finder = new AgentFinder(global1, channel);

		AgentFinder.Response response = finder.goAndWait();

		System.out.println("found agent Info " + response.agentInfo + " from server " + response.globalInfo);

		// now, install an agent on server @2 
		
		SimpleAgent sagent = new SimpleAgent(channel, global2);
		// install into global2
		global2.timeoutCache.put("sagent", sagent, 1 * 1000, () -> {
			System.out.println(" SimpleAgent #2 timed out ! called from " + Thread.currentThread());
		});
		global2.subscribe(sagent, sagent.sub);
		
		// try the finder again
		
		  finder = new AgentFinder(global1, channel);

		  response = finder.goAndWait();

		System.out.println("found agent Info " + response.agentInfo + " from server " + response.globalInfo);

	}

	boolean failed = false;

	boolean received = false;

	@Test
	public void demoOneX() throws IOException {

		// in this thread, echo for a channel.

		String channel = "AbcDefHijQQQQQ";

		String someReplyChannel = "DearJohnIveBeenSoBusy";

		SimpleAgent localWatcher = new SimpleAgent(channel, global1) {
			public void run(Runnable message) {
				if (message instanceof AgentEcho) {
					received = true;
					// this.notify();
					System.out.println(" ****   ****   ****   ****   ****   ****  localWatcher called from " + Thread.currentThread());
				}
			}
		};

		AgentEcho echo = new AgentEcho(someReplyChannel);

		// subscribe to back channel
		global1.subscribe(localWatcher, someReplyChannel);
		global1.timeoutCache.put("localWatcher", localWatcher, 10, () -> {
			failed = true;
			System.out.println(" timeoutCache called from " + Thread.currentThread());
		});

		global1.publish(channel, echo);

		// we should get back ??
		while (failed == false && received == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		// remove global1 agent
		global1.timeoutCache.remove("localWatcher");

		System.out.println(" failed = " + failed);
		System.out.println(" received = " + received);
		Assert.assertTrue(failed);
		Assert.assertFalse(received);

		// reset
		failed = false;
		received = false;

		// install the agent in global2
		SimpleAgent sagent = new SimpleAgent(channel, global2);
		// install into global2
		global2.timeoutCache.put("sagent", sagent, 1 * 1000, () -> {
			System.out.println(" SimpleAgent #2 timed out ! called from " + Thread.currentThread());
		});
		global2.subscribe(sagent, sagent.sub);

		// put it back
		global1.timeoutCache.put("localWatcher", localWatcher, 1000, () -> {
			failed = true;
			System.out.println(" timeoutCache of localWatcher2 called from " + Thread.currentThread());
		});
		// publish the echo again
		global1.publish(channel, echo);

		while (failed == false && received == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		//
		global1.timeoutCache.remove("localWatcher");

		System.out.println(" failed 2 = " + failed);
		System.out.println(" received 2 = " + received);
		// Assert.assertFalse(failed);
		// Assert.assertTrue(received);

	}

	@Test
	public void demoTwo() throws IOException {

		// ReceiveTemperatureDateLoggingAgent tmp = new ReceiveTemperatureDateLoggingAgent("ReceivingAgent123");
		//
		// global1.dynamoHelper.delete(tmp);
		//
		// ReceiveTemperatureDateLoggingAgent agent = (ReceiveTemperatureDateLoggingAgent)
		// global1.dynamoHelper.read(tmp);
		// Assert.assertNull(agent);

		// the logger does not exist in the db, or in the RAM, or in the pubsub.
		// ram and pubsub are virgin.

		/**
		 * Method:
		 * 
		 * The client connects and the server has a ctx. The client may offer the ctx an id and then the ctx may
		 * 
		 */

		MyUser user = new MyUser("Alice#1");
		LogonMessage join = new LogonMessage();
		join.applicationId = LogonMessage.class.getName();
		join.user = user.getId();

		// ObjectNode logon = ServerGlobalState.serialize2node(new LogonMessage());
		//
		// System.out.println(logon);
		clients.get(0).enqueueRunnable(join);

	}

	public static void main(String[] args) throws IOException {

		Global.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		SessionAgent.logger.setLevel(Level.TRACE);
		LogonMessage.logger.setLevel(Level.TRACE);
		SimpleAgent.logger.setLevel(Level.TRACE);
		AgentEcho.logger.setLevel(Level.TRACE);

		TempDataLogger test = new TempDataLogger();

		setup();

		test.demoOne();

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}

		closeAll();
	}

}
