package org.messageweb.util;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.messageweb.ClusterState;
import org.messageweb.Global;
import org.messageweb.messages.Push2Client;
import org.messageweb.messages.Subscribe;

public class StartOneGlobalServer {

	public static Logger logger = Logger.getLogger(StartOneGlobalServer.class);

	public static boolean running = true;

	public static void main(String[] args) {
		
		Push2Client.logger.setLevel(Level.TRACE);
		//Live.logger.setLevel(Level.TRACE);
		Subscribe.logger.setLevel(Level.TRACE);
		
		System.setProperty("catalina.base", "..");

		// No Guice here yet. Assembling manually.

		Global global = new Global(8081, new ClusterState());// starts a ws server

		// publish the time every 10 sec.
		long time_10 = System.currentTimeMillis() + 10 * 1000;
		while (running) {
			long time = System.currentTimeMillis();
			if (time > time_10) {
				time_10 = time + 10 * 1000;
				global.publish("#TimeEveryTenSeconds", new Push2Client("" + new Date()));
				logger.info("sent time to #TimeEveryTenSeconds");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
