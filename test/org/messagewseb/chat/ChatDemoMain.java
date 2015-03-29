package org.messagewseb.chat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.messageweb.ClusterState;
import org.messageweb.ServerGlobalState;
import org.messageweb.WsClientImpl;
import org.messageweb.impl.MyWebSocketClientHandler;
import org.messageweb.messages.PingEcho;
import org.messageweb.socketimpl.MyWebSocketServer;
import org.messageweb.socketimpl.MyWebSocketServerHandler;
import org.messageweb.util.TimeoutCache;
import org.messagewseb.Ping;

public class ChatDemoMain {

	/**
	 * Chat has a board name. A list of users who are online. A list of messages and times.
	 * 
	 * There are notices of leaving and entering. Notices of things posted.
	 * 
	 * Voting system to kick someone. ? Voting system to ban them and remember it ?
	 * 
	 * A memory of a screen-full, or all ever, chat history?
	 * 
	 * make an AllRooms thing?
	 * 
	 * Method:
	 * 
	 * make a room. it listens on a channel.
	 * 
	 * client sends log-on message to room. room sends history. once. room sends user list. once. room sends joined
	 * 
	 * client sends message room sends messages
	 * 
	 * client leaves others get leaving message
	 * 
	 * 
	 */

	ServerGlobalState global;
	List<WsClientImpl> clients;

	@BeforeClass
	public void setup() {
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);

		global = new ServerGlobalState(8081, new ClusterState());// starts a ws server
		// WsClientImpl client = new WsClientImpl(8081);// start a client
		clients = new ArrayList<>();
		clients.add(new WsClientImpl(8081));
		clients.add(new WsClientImpl(8081));
		clients.add(new WsClientImpl(8081));
	}

	@AfterClass
	public void closeAll() {
		global.stop();
		for (WsClientImpl client : clients) {
			client.stop();// aka client.running = false;
		}
	}

	@Test
	public void demoOne() {

		// we need to start a listener on one of the servers.
		// How?
		String channel;
		{ // insert the object.
			Room room = new Room();
			room.name = "Room#2";
			  channel = room.context + room.name;
			room.id = channel;
			global.mapper.save(room);
		}
		
		CreateAgentMessage msg = new CreateAgentMessage();
		msg.channel = channel;
		clients.get(0).enqueueRunnable(msg);

		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
		}

	}

	public static void main(String[] args) {
		
		ServerGlobalState.logger.setLevel(Level.TRACE);
		MyWebSocketServer.logger.setLevel(Level.TRACE);
		WsClientImpl.logger.setLevel(Level.TRACE);
		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
		Ping.logger.setLevel(Level.TRACE);
		PingEcho.logger.setLevel(Level.TRACE);
		TimeoutCache.logger.setLevel(Level.TRACE);
		MyWebSocketServerHandler.logger.setLevel(Level.TRACE);

		ChatDemoMain test = new ChatDemoMain();

		test.setup();

		test.demoOne();

		test.closeAll();

	}

}
