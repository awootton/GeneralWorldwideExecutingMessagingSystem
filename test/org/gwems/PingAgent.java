package org.gwems;

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
import org.gwems.util.TimeoutCache;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.experiments.AgentFinder;
import org.messageweb.testmessages.AgentEcho;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

public class PingAgent extends StartServers {

	@Test
	public void myDemoOne() throws IOException {

		// fixme: add these tests again sometime 
		
		demoAgentPing(global1);// from server1 to server1 - easy
	
		//demoAgentPing(global2);// from server1 to server2 - a little harder
		
		//demoAgentPing(global4);// from server1 to server3 - different clusters - broken TODO: FIXME: 
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


	public static void main(String[] args)   {

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

		PingAgent test = new PingAgent();

		setup();

		try {
			test.myDemoOne();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}

		closeAll();
	}
}
