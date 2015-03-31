package org.messagewseb.common.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.messageweb.ClusterState;
import org.messageweb.ServerGlobalState;
import org.messageweb.WsClientImpl;

public class StartServers {
	
	// TODO: needs multiple cluster sim
	
	protected static ServerGlobalState global1;
	protected static ServerGlobalState global2;
	protected static List<WsClientImpl> clients;

	@BeforeClass
	public static void setup() {
		WsClientImpl.logger.setLevel(Level.TRACE);
//		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
//		Ping.logger.setLevel(Level.TRACE);
//		PingEcho.logger.setLevel(Level.TRACE);
//		TimeoutCache.logger.setLevel(Level.TRACE);

		global1 = new ServerGlobalState(8081, new ClusterState());// starts a ws server
		global2 = new ServerGlobalState(8082, new ClusterState());// starts a ws server
		// WsClientImpl client = new WsClientImpl(8081);// start a client
		clients = new ArrayList<>();
		clients.add(new WsClientImpl(8081));
		clients.add(new WsClientImpl(8082));
		clients.add(new WsClientImpl(8081));
		clients.add(new WsClientImpl(8082));
	}
	
//	public static void signIn( WsClientImpl client, MyUser user ){
		
		// FIXME 
		
//		ObjectNode tmp = ServerGlobalState.serialize2node(new PingEcho());
//		
//		System.out.println(tmp);
//		
//		ObjectNode logon = ServerGlobalState.serialize2node(new LogonMessage());
//		
//		System.out.println(logon);
//		//clients.get(0).enqueueString(logon.toString());
//		
//		//clients.get(0).enqueueRunnable(r);

//	}

	@AfterClass
	public static void closeAll() {
		global1.stop();
		global2.stop();
		for (WsClientImpl client : clients) {
			client.stop();// aka client.running = false;
		}
	}


}
