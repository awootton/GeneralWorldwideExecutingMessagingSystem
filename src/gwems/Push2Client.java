package gwems;


import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Push to client.
 * 
 * {"@C":"gwems.Push2Client","msg":"none"}
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
	
	public Push2Client() {// for jackson
	}

	@Override
	public void run() {
		// meant to run in a session agent.
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if ( logger.isTraceEnabled()){
				logger.trace("Sending message2client " + msg + " to " + session);
			}
			String from = "" + ec.subscribedChannel.get();
			// We should not do this. We should make client write the publish with 'from' in it if that's what they want.
			ObjectNode node = Global.getPlainNode();
			node.put("from", from);
			node.put("msg" , msg);
			session.writeAndFlush("" + node);
		} else {
			// what?
			logger.debug("non session message? " + msg + " agent = " + ec.agent);
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Push2Client("none")));
		System.out.println(Global.serializePretty(new Push2Client("none")));
	}

	

}
