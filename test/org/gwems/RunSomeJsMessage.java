package org.gwems;

import gwems.Js;

import java.io.IOException;

import org.apache.log4j.Level;
import org.gwems.agents.Agent;
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

public class RunSomeJsMessage extends StartServers {

	@Test
	public void someJs() throws IOException, InterruptedException {

		Js scriptMessage = new Js();

		scriptMessage.js += "\n";
		scriptMessage.js += "console.log('global id is ' + global.id)";

		scriptMessage.js += "\n";
		scriptMessage.js += "var touched = 1;";
		scriptMessage.js += "console.log('touched is ' + touched)";

		Agent agent = new SimpleAgent(Global.getRandom(), global1);

		Agent agent2 = new SimpleAgent(Global.getRandom(), global1);

		// won't work: scriptMessage.run();
		// we need to pass it to a global
		// with an agent !!!

		runScript(agent, scriptMessage);

		Object obj = agent.bindings.get("touched");
		System.out.println("touched = " + obj);
		Assert.assertEquals("1", "" + obj);// th

		scriptMessage.js = "console.log('touched is now ' + touched);\n";
		scriptMessage.js += "var myAgentId = '" + agent.getKey() + "';\n";

		System.out.println(scriptMessage.js);
		runScript(agent, scriptMessage);
		
		obj = agent.bindings.get("myAgentId");
		System.out.println("obj = " + obj);
		while (agent.bindings.get("myAgentId") == null) 
			Thread.sleep(1);
		obj = agent.bindings.get("myAgentId");
		Assert.assertEquals(agent.getKey(), "" + obj);
		
		// now, the other agent
		scriptMessage.js = "var myAgentId = '" + agent2.getKey() + "';\n";
		runScript(agent2, scriptMessage);
		obj = agent2.bindings.get("myAgentId");
		System.out.println("obj = " + obj);
	
		while (agent2.bindings.get("myAgentId") == null) 
			Thread.sleep(1);
		obj = agent2.bindings.get("myAgentId");
		Assert.assertEquals(agent2.getKey(), "" + obj);

		// we were supposed to be reusing the same engine all this time,
		Assert.assertEquals(1, global1.getJsEnginePool().getCreatedCount());
	}

	public void runScript(Agent agent, Js scriptMessage) {
		try {
			String randomName = "PgZvDtVnpebKqKhABWLo";
			if (agent.bindings != null)
				agent.bindings.remove(randomName);
			agent.messageQ.run(scriptMessage);
			Js touch = new Js();
			touch.js = "var " + randomName + " = 1;";
			agent.messageQ.run(touch);
			while (agent.bindings == null)
				Thread.sleep(1);
			while (agent.bindings.get(randomName) == null)
				Thread.sleep(1);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

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

		RunSomeJsMessage test = new RunSomeJsMessage();

		setup();

		test.someJs();

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}

		closeAll();
	}
}