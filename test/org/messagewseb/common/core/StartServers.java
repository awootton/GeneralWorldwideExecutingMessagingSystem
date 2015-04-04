package org.messagewseb.common.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class StartServers {
	
	protected static Global global1;
	protected static Global global2;
	protected static Global global3;
	protected static Global global4;
	protected static List<WsClientImpl> clients;
	protected static List<Global> servers;

	@BeforeClass
	public static void setup() {
		WsClientImpl.logger.setLevel(Level.TRACE);
//		MyWebSocketClientHandler.logger.setLevel(Level.TRACE);
//		Ping.logger.setLevel(Level.TRACE);
//		PingEcho.logger.setLevel(Level.TRACE);
//		TimeoutCache.logger.setLevel(Level.TRACE);
		
		ClusterState cluster1 = new ClusterState();
		ClusterState cluster2 = new ClusterState();
		
		cluster1.redis_port = 6379;
		cluster2.redis_port = 6380;

		global1 = new Global(8081,   cluster1);// starts a ws server
		global2 = new Global(8082,   cluster1);// starts a ws server
		global3 = new Global(8083,   cluster2);// starts a ws server
		global4 = new Global(8084,   cluster2);// starts a ws server
		// WsClientImpl client = new WsClientImpl(8081);// start a client
		clients = new ArrayList<>();
		clients.add(new WsClientImpl(8081));
		clients.add(new WsClientImpl(8082));
		clients.add(new WsClientImpl(8083));
		clients.add(new WsClientImpl(8084));
		
		servers = new ArrayList<>();
		servers.add(global1);
		servers.add(global2);
		servers.add(global3);
		servers.add(global4);
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
		for (Global global : servers) {
			global.stop();
		}
		for (WsClientImpl client : clients) {
			client.stop();// aka client.running = false;
		}
	}


}
