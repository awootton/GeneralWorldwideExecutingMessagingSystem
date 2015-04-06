package org.gwems.agents;

import java.util.Optional;

import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;
import org.gwems.util.AgentRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * needs: time to live. 
 * helpers.
 * test with storage.
 * We'll want to save these in db soon. 
 * 
 * @author awootton
 *
 */

@DynamoDBTable(tableName = "Agents")
public abstract class Agent implements Comparable<Agent> {

	/**
	 * We'll need to install the messageQ manually. Fortunately there will only be a handful of places where these are deserialized.
	 */
	@DynamoDBIgnore
	@JsonIgnore
	public AgentRunnablesQueue messageQ = null;

	public String key = "";// aka the  ?, is unique

	public Agent(String key) {
		this.key = key;
//		byte[] bytes = Global.getContext().sha256.digest((sub + "publish").getBytes());
//		String publishchannel = Base64.getEncoder().encodeToString(bytes);
//		pub = publishchannel;
	}

	/**
	 * Sometimes the agent might want to filter the messages.
	 * To do that override this. Otherwise, as you can see, the messages just run.
	 * @param message
	 */
	public void run(Runnable message) {
		message.run();
	}

	static interface Handler {
		public void handle(Runnable message);
	}

	@Override
	public int compareTo(Agent o) {
		return key.compareTo(o.key);
	}

	public static Global getGlobal() {
		ExecutionContext ec = Global.getContext();
		return ec.global;
	}

	/**
	 * If the message came from a subscription channel then it will have one of these.
	 * 
	 * @return
	 */
	public static Optional<String> getChannelSubscriptionString() {
		ExecutionContext ec = Global.getContext();
		return ec.subscribedChannel;
	}

}
