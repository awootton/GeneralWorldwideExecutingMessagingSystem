package org.messageweb.messages;

import java.util.Base64;

import org.apache.log4j.Logger;
import org.messageweb.ServerGlobalState;
import org.messageweb.agents.SessionAgent;

public class LogonMessage implements Runnable {

	public static Logger logger = Logger.getLogger(LogonMessage.class);

	public String user;// eg. Smith Pacific Agency
	public String applicationId;// eg org.mmm.TempLogger
	public String instanceid;// eg.South Shore #18

	// needs crypto

	@Override
	public void run() {

		SessionAgent agent = (SessionAgent) ServerGlobalState.getContext().agent.get();
		logger.trace("found agent " + agent);

		// we have to set up the subscription

		String channel = "some lame channel";

		byte[] bytes = ServerGlobalState.getContext().sha256.digest((user + applicationId + instanceid).getBytes());
		channel = Base64.getEncoder().encodeToString(bytes);

		agent.pub = channel;
		
		// What will be the publish channel for the new agent?? 
//		 bytes = ServerGlobalState.getContext().sha256.digest((channel + "publish").getBytes());
//		String publishchannel = Base64.getEncoder().encodeToString(bytes);
		
		// We need to listen to the publishchannel and send a ping to the channel
		// and wait for 4 seconds. 
		// after 4 seconds we check the db
		// then we make one on this server
		
		

		agent.validated = true;

	}

}
