package org.messageweb.agents;

import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.util.AgentRunnablesQueue;
import org.messageweb.util.SessionRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * All the messages that the client sends pass through here. And, all the messages that are received for the client also pass through here.
 * 
 * Do we really want to do it this way?
 * 
 * 1) If this is the first message then we need to know a handler type to replace it with or we need to know a channel to publish to. And we need to validate
 * creds, maybe, depends on type I guess.
 * 
 * 2) Subscriptions pass through iff validated.
 * 
 * @author awootton
 *
 */
public class SessionAgent extends Agent {

	public static Logger logger = Logger.getLogger(SessionAgent.class);

	public boolean validated = false;

//	public String outgoingChannel = "";

	@DynamoDBIgnore
	@JsonIgnore
	public SessionRunnablesQueue socketMessageQ = null;

	public SessionAgent(Global global, String sub) {
		super(sub);
		messageQ = new AgentRunnablesQueue(global, this);

		socketMessageQ = new SessionRunnablesQueue(global, this);
	}

	// Handler handleSock2 = (message) -> {
	//
	// logger.info("have handleSock2 message " + message);
	// };
	//
	// Handler handleSocket = (message) -> {
	// // is there an application?
	// if (message instanceof LogonMessage) {
	// String userid = ((LogonMessage) message).user;
	// String appid = ((LogonMessage) message).applicationId;
	// MyUser user = (MyUser) getGlobal().dynamoHelper.read(new MyUser(userid));// blocks
	//
	// logger.trace("socket " + userid + " " + appid + " " + user);
	//
	// handleSocket = handleSock2;
	//
	// } else
	// logger.trace("handleSocket  " + message);
	// };
	//
	// Handler handleSubscriptions = (message) -> {
	//
	// logger.info("have sub message " + message);
	// };

	 
	public void runSocketMessage(Runnable message) {
		if ( validated ){
			//getGlobal().publish(this.pub, message);
		} else {
		  message.run();
		}
	}

	@Override
	public void run(Runnable message) {

		message.run();

		// Optional<String> channel = getChannelSubscriptionString();
		// logger.trace("message " + message + " on " + channel );
		// if (channel.isPresent()) {
		// handleSubscriptions.handle(message);
		// } else if (getCtxSessionAgent().isPresent()) {
		// handleSocket.handle(message);
		// }
	}

}
