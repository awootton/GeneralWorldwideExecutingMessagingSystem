package org.gwems;

import gwems.Js;

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
import org.junit.Test;
import org.messageweb.experiments.AgentFinder;
import org.messageweb.testmessages.AgentEcho;
import org.messageweb.testmessages.LogonMessage;
import org.messageweb.testmessages.PingEcho;

public class RunSomeJsMessage extends StartServers {

	@Test
	public void someJs() throws IOException {

		Js scriptMessage = new Js();

		scriptMessage.js += "\n";
		scriptMessage.js += "console.log('global id is ' + global.id)";

		scriptMessage.js += "\n";
		scriptMessage.js += "console.log('global id is ' + global.id)";

		// won't work scriptMessage.run();
		// we need to pass it to a global
		global1.execute(scriptMessage);

	}

	public static void main(String[] args) throws IOException {

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