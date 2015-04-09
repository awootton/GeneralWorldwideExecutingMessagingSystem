package gwems;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/**
 * Clients send this: {"@C":"gwems.Unsubscribe","channel":"none"} to start a subscription towards their SessionAgent.
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
		if (ec.agent.isPresent()) {
			if (channel.length() >= 4 && !"none".equals(channel)) {
				ec.global.unsubscribe(ec.agent.get(), channel.trim());
				if (logger.isDebugEnabled())
					logger.debug("Session" + ec.getAgentName() + " unsubscribed to " + channel);
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
