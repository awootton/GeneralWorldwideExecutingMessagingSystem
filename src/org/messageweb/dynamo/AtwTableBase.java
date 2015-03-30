package org.messageweb.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AtwTable2")
public class AtwTableBase {

	String id = "dummyKey123";

	public AtwTableBase(String key) {
		this.id = key;
	}

	@DynamoDBHashKey(attributeName = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKey(String id) {
		this.id = id;
	}

}
