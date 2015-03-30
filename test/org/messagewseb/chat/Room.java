package org.messagewseb.chat;

import java.util.ArrayList;
import java.util.List;

import org.messageweb.dynamo.AtwTableBase;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AtwTable2")
public class Room extends AtwTableBase {
	

	final String context = "Agents:demo:chat1:";
	
	String name = "Room#1";

	List<String> users = new ArrayList<>();
	
	// List<Message> messages = new ArrayList<>();
	List<String> messages = new ArrayList<>();
	
	public Room(String key) {
		super(key);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	

}
