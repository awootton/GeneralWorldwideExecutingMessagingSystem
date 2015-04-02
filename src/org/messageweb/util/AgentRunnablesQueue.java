package org.messageweb.util;

import java.util.Optional;

import org.messageweb.ExecutionContext;
import org.messageweb.Global;
import org.messageweb.agents.Agent;

public class AgentRunnablesQueue extends RunnablesQueue {

	Agent agent;

	public AgentRunnablesQueue(Global global, Agent agent) {
		super(global);
		this.agent = agent;
	}

	protected Runnable getLocalRunner() {
		return new MyAgentLocalRunner();
	}

	private class MyAgentLocalRunner extends MyLocalRunner {
		@Override
		public void run() {
			ExecutionContext context = Global.getContext();
			// was wrapped by caller 
			// do it again anyway.
			context.agent = Optional.of(agent);
			//System.out.println(" ######### SEtting context for " + agent);
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
