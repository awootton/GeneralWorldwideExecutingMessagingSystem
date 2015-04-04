package org.gwems;


import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/** Push to client.
 * 
 * {"@C":"org.gwems.Push2Client","msg":"none"}
 * 
 * @author awootton
 *
 */
public class Push2Client implements Runnable {

	public static Logger logger = Logger.getLogger(Push2Client.class);

	String msg = "none";

	public Push2Client(String message) {
		super();
		this.msg = message;
	}

	@Override
	public void run() {
		// meant to run in a session agent.
		ExecutionContext ec = Global.getContext();
		// Global global = ec.global;
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if ( logger.isTraceEnabled()){
				logger.trace("Sending message " + msg + " to " + session.key);
			}
			session.writeAndFlush(msg);
		} else {
			// what?
			logger.debug("non session message? " + msg + " agent = " + ec.agent);
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Push2Client("none")));
	}

	

}
