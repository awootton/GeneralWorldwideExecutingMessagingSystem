package org.messageweb.agents;

import java.util.Base64;
import java.util.Optional;

import org.messageweb.ExecutionContext;
import org.messageweb.ServerGlobalState;
import org.messageweb.util.AgentRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnore;

// interface?? abstract ?? 

/**
 * 
 * needs: time to live. 
 * helpers.
 * test with storage.
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

	public String sub = "";// aka the subscription channel
	public String pub = "";// aka the publish channel

	public Agent(String key) {
		sub = key;
		byte[] bytes = ServerGlobalState.getContext().sha256.digest((sub + "publish").getBytes());
		String publishchannel = Base64.getEncoder().encodeToString(bytes);
		pub = publishchannel;
	}

	@DynamoDBHashKey(attributeName = "sub")
	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	@DynamoDBHashKey(attributeName = "sub")
	public String getPub() {
		return pub;
	}

	public void setPub(String pub) {
		this.pub = pub;
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
		return sub.compareTo(o.sub);
	}

	/**
	 * If the message came from a client socket, or any socket, then it has a session id.
	 * 
	 * @return
	 */
	public static Optional<Agent> getCtxSessionAgent() {
		ExecutionContext ec = ServerGlobalState.getContext();
		return ec.agent;
	}

	public static ServerGlobalState getGlobal() {
		ExecutionContext ec = ServerGlobalState.getContext();
		return ec.global;
	}

	/**
	 * If the message came from a subscription channel then it will have one of these.
	 * 
	 * @return
	 */
	public static Optional<String> getChannelSubscriptionString() {
		ExecutionContext ec = ServerGlobalState.getContext();
		return ec.subscribedChannel;
	}

}
