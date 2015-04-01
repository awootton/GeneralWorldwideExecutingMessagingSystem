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
import org.messageweb.experiments.AgentFinder;
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
	 */

	@Test
	public void demoOne() throws IOException {
		demoAgentPing(global1);
		demoAgentPing(global2);
	}

	public void demoAgentPing(Global targetServer) throws IOException {

		// ping for agent. Should fail.

		String channel = "AbcDefHijQQQQQ";
		// Don't reuse the id on short notice.
		// Is this a flaw?
		channel = Global.getRandom();

		AgentFinder finder = new AgentFinder(global1, channel);
		AgentFinder.Response response = finder.goAndWait();

		System.out.println("found agent Info " + response.agentInfo + " from server " + response.globalInfo);

		Assert.assertTrue(response.failed);
		Assert.assertFalse(response.success);

		// now, install an agent on server

		SimpleAgent simpleAgent = new SimpleAgent(channel, targetServer);
		// install into globalX
		// FIXME: formalize agentInstall
		targetServer.timeoutCache.put("simpleAgentAAAA", simpleAgent, 2500, () -> {
			System.out.println(" SimpleAgent #2 timed out ! called from " + Thread.currentThread());
		});
		targetServer.subscribe(simpleAgent, channel);

		// try the finder again
		// should succeed
		finder = new AgentFinder(global1, channel);
		response = finder.goAndWait();

		System.out.println("found agent Info " + response.agentInfo + " from server " + response.globalInfo);

		Assert.assertTrue(response.success);
		Assert.assertFalse(response.failed);
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

		AgentFinder.logger.setLevel(Level.TRACE);

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
