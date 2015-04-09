package gwems;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MultiPublish implements Runnable {

	public static Logger logger = Logger.getLogger(Publish.class);

	public List<String> channels = new ArrayList<>();// Collections.emptyList();
	public Runnable msg = new Push2Client("none");

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		for (String channel : channels) {
			if (channel.length() >= 3 && !"na".equals(channel)) {
				// we're NOT going to wrap the message in a Push2Client
				// someone else had to already wrap it.
				ec.global.publish(channel.trim(), msg);
				if (logger.isTraceEnabled())
					logger.trace("Session" + Global.getContext().getAgentName() + " did publish to " + channel);
			} else {
				logger.error("channel name was too short, or none:" + channel);
			}
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		MultiPublish test = new MultiPublish();
		test.channels.add("#mom");
		test.channels.add("#lovePuppies");
		System.out.println(Global.serialize(test));

	}
}