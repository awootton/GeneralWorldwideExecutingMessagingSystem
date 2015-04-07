package org.gwems;

import java.io.IOException;

import org.apache.log4j.Level;
import org.gwems.agents.SessionAgent;
import org.gwems.agents.SimpleAgent;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.servers.impl.JedisRedisPubSubImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.servers.impl.MyWebSocketClientHandler;
import org.gwems.temperature.TempDataLogger;
import org.gwems.util.TimeoutCache;
import org.junit.Test;
import org.messageweb.experiments.AgentFinder;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.socketimpl.MyWebSocketServerHandler;
import org.messageweb.testmessages.AgentEcho;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

public class PingAgent extends TempDataLogger {

	@Test
	public void myDemoOne() throws IOException {
		demoAgentPing(global1);// from server1 to server1 - easy
		demoAgentPing(global2);// from server1 to server2 - a little harder
		
		//demoAgentPing(global4);// from server1 to server3 - different clusters - broken TODO: FIXME: 
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
		
		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);

		PingAgent test = new PingAgent();

		setup();

		test.myDemoOne();

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}

		closeAll();
	}
}
