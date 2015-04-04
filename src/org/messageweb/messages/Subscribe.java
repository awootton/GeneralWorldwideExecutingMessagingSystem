package org.messageweb.messages;

import org.apache.log4j.Logger;
import org.messageweb.ExecutionContext;
import org.messageweb.Global;
import org.messageweb.agents.SessionAgent;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Clients send this: {"@Cl":"org.messageweb.messages.Subscribe","channel":"none"} to start a subscription towards
 * their SessionAgent.
 * 
 * There is no reply.
 * 
 * @author awootton
 *
 */
public class Subscribe implements Runnable {
	
	public static Logger logger = Logger.getLogger(Subscribe.class);

	public String channel = "none";

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();

			if (channel.length() >= 4 && !"none".equals(channel)) {
				ec.global.subscribe(session, channel);
				logger.info("Session" + session.key + " subscribed to " + channel);
			}
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Subscribe()));
	}

}