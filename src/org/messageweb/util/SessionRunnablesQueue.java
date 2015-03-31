package org.messageweb.util;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.messageweb.ExecutionContext;
import org.messageweb.ServerGlobalState;
import org.messageweb.agents.SessionAgent;

public class SessionRunnablesQueue  extends RunnablesQueue {

	SessionAgent agent;

	public SessionRunnablesQueue(Executor executor, SessionAgent agent) {
		super(executor);
		this.agent = agent;
	}

	protected Runnable getLocalRunner() {
		return new MyAgentLocalRunner();
	}

	private class MyAgentLocalRunner extends MyLocalRunner {
		@Override
		public void run() {
			//ExecutionContext context = ServerGlobalState.getContext();
			// was wrapped by caller context.agent  = Optional.of(agent);
			// super.run();
			myThread = Thread.currentThread().getName();
			Runnable r;
			while ((r = hasMore()) != null) {
				try {
					// r.run();
					agent.runSocketMessage(r);
				} catch (Exception e) {
					logger.error("badness", e);
				}
			}
			myThread = "off";
			// context.agent = Optional.empty();
		}
	}
}
