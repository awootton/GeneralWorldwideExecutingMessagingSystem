package org.gwems.util;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gwems.Publish;
import org.gwems.Push2Client;
import org.gwems.Subscribe;
import org.gwems.Unsubscribe;
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;

public class StartOneGlobalServer {

	public static Logger logger = Logger.getLogger(StartOneGlobalServer.class);

	public static boolean running = true;

	public static void main(String[] args) {
		
		Push2Client.logger.setLevel(Level.TRACE);
		Subscribe.logger.setLevel(Level.TRACE);
		Unsubscribe.logger.setLevel(Level.TRACE);
		Publish.logger.setLevel(Level.TRACE);
		Global.logger.setLevel(Level.TRACE);
		
		System.setProperty("catalina.base", "..");

		// No Guice here yet. Assembling manually.

		Global global = new Global(8081, new ClusterState());// starts a ws server

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
				global.publish("WWC" + "#TimeEveryMinute", new Push2Client("" + new Date()));
				logger.info("sent time to #TimeEveryMinute");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
