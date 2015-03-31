package org.messageweb;

import org.apache.log4j.Logger;

public class RunServersLocal {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RunServersLocal.class);

	public static boolean running = true;

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		System.setProperty("catalina.base", "..");

		// No Guice here yet. Assembling manually.

		Global global = new Global(8081, new ClusterState());// starts a ws server

		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

	}
}
