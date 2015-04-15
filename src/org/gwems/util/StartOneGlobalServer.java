package org.gwems.util;

import gwems.Publish;
import gwems.Push2Client;
import gwems.Subscribe;
import gwems.Unsubscribe;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;
import org.gwems.servers.impl.MyWebSocketServerHandler;
import org.gwems.servers.impl.WsLoggingHandler;

public class StartOneGlobalServer {

	public static Logger logger = Logger.getLogger(StartOneGlobalServer.class);

	public static boolean running = true;

	public static void main(String[] args) {
		
		Push2Client.logger.setLevel(Level.DEBUG);
		Subscribe.logger.setLevel(Level.TRACE);
		Unsubscribe.logger.setLevel(Level.TRACE);
		Publish.logger.setLevel(Level.DEBUG);
		Global.logger.setLevel(Level.DEBUG);
		
		WsLoggingHandler.logger.setLevel(Level.DEBUG);
		
		MyWebSocketServerHandler.logger.setLevel(Level.DEBUG);
		
		System.setProperty("catalina.base", "..");

		// No Guice here yet. Assembling manually.

		Global global = new Global(8081, new ClusterState());// starts a ws server
		
		//global.sessionTtl = 30000;// 30 sec. 

		// publish the time every 10 sec.
		long time_10 = System.currentTimeMillis() + 10 * 1000;
		long time_60 = System.currentTimeMillis() + 22 * 1000;
		while (running) {
			long time = System.currentTimeMillis();
			if (time > time_10) {
				time_10 += 10 * 1000;
				//global.publish("WWC" + "#TimeEveryTenSeconds", new Push2Client("" + new Date()));
				//global.publish("WWC" + "#10secs", new Push2Client("" + new Date()));
				//logger.info("sent time to #TimeEveryTenSeconds");
			}
			if (time > time_60) {
				time_60 += 60 * 1000;
				global.publish("WWC#TimeEveryMinute", new Push2Client("" + new Date()));
				logger.info("sent time to #TimeEveryMinute");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
