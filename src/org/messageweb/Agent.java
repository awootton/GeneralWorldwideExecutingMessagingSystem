package org.messageweb;

import org.messageweb.util.AgentRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

// interface?? abstract ?? 

public abstract class Agent implements Comparable<Agent> { //extends SerialRunnables implements Comparable<Agent> {
	
	// We'll need to install this manually. 
	@DynamoDBIgnore
	@JsonIgnore
	public AgentRunnablesQueue messageQ = null;
	
	
}
