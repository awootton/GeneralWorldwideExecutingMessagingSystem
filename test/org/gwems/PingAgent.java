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
import org.gwems.util.Stopwatch;
import org.gwems.util.TimeoutCache;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.testmessages.AgentReply;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

/** This used to be a major test but it's getting antique 3/2015
 * The AgentFinder is a bad pattern. 
 * @author awootton
 *
 */
public class PingAgent extends StartServers {

	@Test
	public void myDemoOne() throws IOException {
// FIXME: This fails sometimes but I'm tired tonight. 
		
		//demoAgentPing(global0);// from server0 to server0 - easy
	
		//demoAgentPing(global1);// from server0 to server1 - a little harder
		
		demoAgentPing(global3);// from server0 to server3 - different clusters - hard
	}
	
	int counter = 0;
	
	public void demoAgentPing(Global targetServer) throws IOException {

		// ping for agent. Should fail.

		String channel = "AbcDefHijQQQQQ";
		// Don't reuse the id on short notice.
		// Is this a flaw?
		//channel = Global.getRandom();

		AgentFinder finder = new AgentFinder(global0, channel);
		AgentFinder.Response response = finder.goAndWait();

		System.out.println("found agent Info " + response.agentInfo + " from server " + response.globalInfo);

		Assert.assertTrue(response.failed);
		Assert.assertFalse(response.success);

		// now, install an agent on server
		
		counter = 0;//
		Stopwatch.tryAwhile(1, () -> {
			System.out.println();
			return counter++ < 5;
		});

		// make an agent that is not a socket
		SimpleAgent simpleAgent = new SimpleAgent(targetServer, channel );
		// it subs itself to itself

		// install into globalX
		// FIXME: formalize agentInstall
		targetServer.timeoutCache.put(channel, simpleAgent, 5000, () -> {
			System.out.println(" SimpleAgent #2 timed out ! called from " + Thread.currentThread());
		});
		
		// how do we wait for simpleAgent to finish it's sub?? 
		Stopwatch.tryAwhile(0.1, () -> true);

		// try the finder again
		// should succeed
		finder = new AgentFinder(global0, channel);
		response = finder.goAndWait();

		System.out.println("found agent Info2 " + response.agentInfo + " from server " + response.globalInfo);

		Assert.assertTrue(response.success);
		Assert.assertFalse(response.failed);
	}


	public static void main(String[] args)   {

		Global.logger.setLevel(Level.TRACE);
		//MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		//MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		PingTest.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		//MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		SessionAgent.logger.setLevel(Level.TRACE);
		LogonMessage.logger.setLevel(Level.TRACE);
		SimpleAgent.logger.setLevel(Level.TRACE);
		AgentEcho.logger.setLevel(Level.TRACE);

		AgentFinder.logger.setLevel(Level.TRACE);
		
		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);
		AgentReply.logger.setLevel(Level.TRACE);
		
		PingAgent test = new PingAgent();

		setup();

		try {
			test.myDemoOne();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		closeAll();
	}
}
