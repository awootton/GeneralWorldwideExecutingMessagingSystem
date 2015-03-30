package org.messageweb.util;

import java.util.concurrent.Executor;

import org.messageweb.Agent;
import org.messageweb.ExecutionContext;
import org.messageweb.ServerGlobalState;

public class AgentRunnablesQueue extends RunnablesQueue {

	Agent agent;

	public AgentRunnablesQueue(Executor executor, Agent agent) {
		super(executor);
		this.agent = agent;
	}

	protected Runnable getLocalRunner() {
		return new MyAgentLocalRunner();
	}

	private class MyAgentLocalRunner extends MyLocalRunner {
		@Override
		public void run() {
			ExecutionContext context = ServerGlobalState.getContext();
			context.agent = agent;
			super.run();
			context.agent = null;
		}
	}
}
