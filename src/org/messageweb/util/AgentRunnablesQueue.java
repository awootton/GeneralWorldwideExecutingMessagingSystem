package org.messageweb.util;

import java.util.concurrent.Executor;

import org.messageweb.agents.Agent;

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
			//ExecutionContext context = ServerGlobalState.getContext();
			// was wrapped by caller context.agent = Optional.of(agent);
			myThread = Thread.currentThread().getName();
			Runnable r;
			while ((r = hasMore()) != null) {
				try {
					agent.run(r);
				} catch (Exception e) {
					logger.error("badness", e);
				}
			}
			myThread = "off";
			//context.agent = Optional.empty();
		}
	}
}
