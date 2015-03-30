package org.messagewseb.temperature;

import java.util.ArrayList;
import java.util.List;

import org.messageweb.dynamo.AtwTableBase;

public class ReceivingAgent extends AtwTableBase{

	public ReceivingAgent(String key) {
		super(key);
	}

//	String id = "ReceivingAgent123";
	
	List<String> values = new ArrayList<>();

	public String getDataLogUrl() {
		// do I have an S3 bucket to append to? 
		return "unknown";
	}
	
	public void addValue(String val ){
		
	}

}
