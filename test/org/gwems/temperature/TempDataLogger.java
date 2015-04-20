package org.gwems.temperature;

import java.io.IOException;

import org.apache.log4j.Level;
import org.gwems.PingTest;
import org.gwems.agents.SessionAgent;
import org.gwems.agents.SimpleAgent;
import org.gwems.common.core.StartServers;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.MyWebSocketClientHandler;
import org.gwems.servers.impl.MyWebSocketServer;
import org.gwems.servers.impl.MyWebSocketServerHandler;
import org.gwems.util.TimeoutCache;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.dynamo.MyUser;
import org.messageweb.experiments.AgentFinder;
import org.messageweb.testmessages.AgentEcho;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;


/** so like totally wip that it's actually crap that needs to be flushed.
 * 
 * @author awootton
 *
 */
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
//		demoAgentPing(global1);
	//	demoAgentPing(global2);
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

	public static void main(String[] args)  {

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

		TempDataLogger test = new TempDataLogger();

		setup();

		try {
			test.demoOne();
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
