package org.gwems;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Clients send this: {"@C":"org.gwems.Unsubscribe","channel":"none"} to start a subscription towards their
 * SessionAgent.
 * 
 * There is no reply.
 * 
 * @author awootton
 *
 */
public class Unsubscribe implements Runnable {

	public static Logger logger = Logger.getLogger(Unsubscribe.class);

	public String channel = "none";

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if (channel.length() >= 4 && !"none".equals(channel)) {
				ec.global.unsubscribe(session, "WWC" + channel.trim());
				if (logger.isDebugEnabled())
					logger.debug("Session" + session.key + " unsubscribed to " + channel);
			}
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Unsubscribe()));
		System.out.println(Global.serializePretty(new Unsubscribe()));
	}

}
