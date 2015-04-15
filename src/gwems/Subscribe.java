package gwems;

import org.apache.log4j.Logger;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Clients send this: {"@":"gwems.Subscribe","channel":"none"} to start a subscription towards their SessionAgent.
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
		if (ec.agent.isPresent() && (channel.length() >= 4 && !"none".equals(channel))) {
			ec.global.subscribe(ec.agent.get(), channel.trim());
			if (logger.isDebugEnabled())
				logger.debug("Session" + ec.getAgentName() + " subscribed to " + channel);
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Subscribe()));
		System.out.println(Global.serializePretty(new Subscribe()));
	}

}
