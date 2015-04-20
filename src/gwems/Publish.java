package gwems;

import org.apache.log4j.Logger;
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
 * {"@":"gwems.Publish","channel":"none","msg":{"@":"gwems.Push2Client","msg":"msg"}}
 * 
 * @author awootton
 *
 */
public class Publish implements Runnable {

	public static Logger logger = Logger.getLogger(Publish.class);

	public String channel = "na";
	public Runnable msg = new Push2Client("none");
	
	public Publish(){
		
	}

	public Publish(String channel, Runnable msg) {
		super();
		this.channel = channel;
		this.msg = msg;
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
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

	public static void main(String[] args) throws JsonProcessingException {

		Publish p = new Publish();
		System.out.println(Global.serialize(p));
		System.out.println(Global.serializePretty(p));

	}

}
