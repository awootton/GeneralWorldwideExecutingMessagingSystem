package org.messageweb.rest;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AtwTable2")
public class LastTimeItem {
	String id = "dummyKey123";
	String when = "none";

	public LastTimeItem(String id) {
		super();
		this.id = id;
	}

	public LastTimeItem() {// required for Dynamo 
	}

	@DynamoDBHashKey(attributeName = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@DynamoDBAttribute(attributeName = "when")
	public String getWhen() {
		return when;
	}

	public void setWhen(String last) {
		this.when = last;
	}

}