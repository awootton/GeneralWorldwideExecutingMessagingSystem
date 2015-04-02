package org.messageweb.util;

import org.messageweb.Global;
import org.messageweb.agents.SessionAgent;

public class SessionRunnablesQueue  extends RunnablesQueue {

	SessionAgent agent;

	public SessionRunnablesQueue(Global global, SessionAgent agent) {
		super(global);
		this.agent = agent;
	}

	protected Runnable getLocalRunner() {
		return new MyAgentLocalRunner();
	}

	private class MyAgentLocalRunner extends MyLocalRunner {
		@Override
		public void run() {
			myThread = Thread.currentThread().getName();
			Runnable r;
			while ((r = hasMore()) != null) {
				try {
					agent.runSocketMessage(r);
				} catch (Exception e) {
					logger.error("badness", e);
				}
			}
			myThread = "off";
		}
	}
}
