package org.messageweb;

public class RunServersLocal {

	public static boolean running = true;

	public static void main(String[] args) {

		System.setProperty("catalina.base", "..");

		// No Guice here yet. Assembling manually.

		ServerGlobalState global = new ServerGlobalState(8081);// starts a ws

		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

	}
}
