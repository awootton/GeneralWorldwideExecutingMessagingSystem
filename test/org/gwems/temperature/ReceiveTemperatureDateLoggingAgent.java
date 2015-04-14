package org.gwems.temperature;

import java.util.ArrayList;
import java.util.List;

import org.gwems.agents.Agent;
import org.gwems.servers.Global;

public class ReceiveTemperatureDateLoggingAgent extends Agent {

	public ReceiveTemperatureDateLoggingAgent(Global global, String key) {
		super(global, key);
	}

	// String id = "ReceivingAgent123";

	List<String> values = new ArrayList<>();

	public String getDataLogUrl() {
		// do I have an S3 bucket to append to?
		return "unknown";
	}

	public void addValue(String val) {
		values.add(val);
	}

}
