package gwems;

import java.io.IOException;

import m.L;
import m.T;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Push to client.
 * 
 * {"@":"gwems.Push2Client","msg":"none"}
 * 
 * @author awootton
 *
 */

public class Push2Client implements Runnable {

	public static Logger logger = Logger.getLogger(Push2Client.class);

	Object msg = "none";

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
			if (logger.isTraceEnabled()) {
				logger.trace("Sending message2client " + msg + " to " + session);
			}
			String from = ec.subscribedChannel.get();
			// We should not do this. We should make client write the publish with 'from' in it if that's what they
			// want.
//			T node = new T();
//			node.put("from", from);
//			node.put("msg", "" + msg);
 			try {
				session.writeAndFlush(Global.serialize(msg));//Global.serialize(node));
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
		} else {
			// what?
			logger.debug("non session message? " + msg + " agent = " + ec.agent);
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println(Global.serialize(new Push2Client("none")));
		System.out.println(Global.serializePretty(new Push2Client("none")));

		Push2Client test = new Push2Client();

		T mnode = new T();
		T posn = new T();
		posn.put("x", 1.0);
		posn.put("y", 123);
		posn.put("z", -1.0);
		mnode.put("position", posn);
		mnode.put("list", new L());
		test.msg = mnode;

		String json = Global.serializePretty(test);
		System.out.println(json);

		Object obj = Global.deserialize(json);
		System.out.println(obj);

	}

}
