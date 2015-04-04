package org.messageweb.messages;

import org.apache.log4j.Logger;
import org.messageweb.ExecutionContext;
import org.messageweb.Global;
import org.messageweb.agents.SessionAgent;

public class Push2Client implements Runnable {

	public static Logger logger = Logger.getLogger(Push2Client.class);

	String message = "none";

	public Push2Client(String message) {
		super();
		this.message = message;
	}

	@Override
	public void run() {
		// meant to run in a session agent.
		ExecutionContext ec = Global.getContext();
		// Global global = ec.global;
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if ( logger.isTraceEnabled()){
				logger.trace("Sending message " + message + " to " + session.key);
			}
			session.writeAndFlush(message);
		} else {
			// what?
			logger.debug("non session message? " + message + " agent = " + ec.agent);
		}
	}
	

}
