package org.messageweb.util;

import java.util.Optional;

import org.messageweb.ExecutionContext;
import org.messageweb.Global;
import org.messageweb.agents.SessionAgent;

/** This is only used one place: SessionAgent
 * 
 * @author awootton
 *
 */
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
					ExecutionContext ec = Global.getContext();
					ec.agent = Optional.of(agent);
					agent.runSocketMessage(r);
					ec.agent = Optional.empty();
				} catch (Exception e) {
					logger.error("badness", e);
				}
			}
			myThread = "off";
		}
	}
}
