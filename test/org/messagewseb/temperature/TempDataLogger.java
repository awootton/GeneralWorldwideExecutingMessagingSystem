package org.messagewseb.temperature;

import java.io.IOException;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.messageweb.ServerGlobalState;
import org.messageweb.WsClientImpl;
import org.messageweb.dynamo.MyUser;
import org.messageweb.impl.MyWebSocketClientHandler;
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
	 * @throws IOException 
	 * 
	 * 
	 * 
	 */

	public void demoOne() throws IOException {
		
		MyUser user = new MyUser("Alice#1");
		signIn(clients.get(0),user);
		
		ReceivingAgent tmp = new ReceivingAgent("ReceivingAgent123");
		
		global1.dynamoHelper.delete(tmp);
		
		ReceivingAgent agent = (ReceivingAgent) global1.dynamoHelper.read(tmp);
		Assert.assertNull(agent);
		
		// the logger does not exist in the db, or in the RAM, or in the pubsub.
		// ram and pubsub are virgin.
		
		
		

	}

	public static void main(String[] args) throws IOException {
		ServerGlobalState.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		TempDataLogger test = new TempDataLogger();

		test.setup();

		test.demoOne();
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}


		test.closeAll();
	}

}
