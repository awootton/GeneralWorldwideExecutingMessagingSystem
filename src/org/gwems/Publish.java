package org.gwems;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This is a message for a client to send which, when run in a SessionAgent, causes a push to happen in a channel.
 * Normally the message arriving at the end of the sub will be useless so we wrap the message in a Push2Client.
 * 
 * See example in main. Note that I'm tacking on a little namespace for the future even though that might be useless.
 * (WWC)
 * 
 * {"@C":"org.gwems.Publish","channel":"none","msg":{"@C":"org.gwems.Push2Client","msg":"msg"}}
 * 
 * @author awootton
 *
 */
public class Publish implements Runnable {

	public static Logger logger = Logger.getLogger(Publish.class);

	public String channel = "na";
	public Runnable msg = new Push2Client("somemsg");

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if (channel.length() >= 3 && !"none".equals(channel)) {
				// we're NOT going to wrap the message in a Push2Client
				// someone else had to already wrap it.
				ec.global.publish("WWC" + channel.trim(), msg);
				if (logger.isDebugEnabled())
					logger.debug("Session" + session.key + " published to " + channel);
			}
		}
	}

	public static void main(String[] args) throws JsonProcessingException {

		Publish p = new Publish();
		System.out.println(Global.serialize(p));
		System.out.println(Global.serializePretty(p));

	}

}
