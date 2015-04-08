package gwems;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Clients send this: {"@C":"gwems.Unsubscribe","channel":"none"} to start a subscription towards their
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
				ec.global.unsubscribe(session, channel.trim());
				if (logger.isDebugEnabled())
					logger.debug("Session" + session + " unsubscribed to " + channel);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println(Global.serialize(new Unsubscribe()));
		System.out.println(Global.serializePretty(new Unsubscribe()));
		Global.deserialize("{\"@C\":\"gwems.Unsubscribe\",\"channel\":\"none\"}");
		// I wish: Global.deserialize("{@C:\"gwems.Unsubscribe\",channel:\"none\"}");
	}

}
