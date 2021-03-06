package org.gwems.servers;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.gwems.agents.Agent;

/**
 * This will be available as ThreadLocal storage.
 * 
 * These could be Optional. Note that null means 'absent', or na.
 * 
 * @author awootton
 *
 */
public class ExecutionContext {

	public ExecutionContext() {
		try {
			md5 = MessageDigest.getInstance("MD5");
			sha = MessageDigest.getInstance("SHA-1");
			sha256 = MessageDigest.getInstance("SHA-256");
			utf8 = Charset.forName("utf8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	// this should NEVER be null
	public Global global;

	// public Optional<ChannelHandlerContext> ctx = Optional.empty();

	public Optional<String> subscribedChannel = Optional.empty();

	public Optional<Agent> agent = Optional.empty();

	public Optional<String> timedObject = Optional.empty();;

	// these are always legit.
	public MessageDigest md5;
	public MessageDigest sha;
	public MessageDigest sha256;
	
	public Charset utf8;

	// A utility.
	public String getAgentName() {
		String name = "unknown";
		if (agent.isPresent()) {
			name = agent.get().getKey();
		}
		return name;
	}

	byte[] lastRandom = new byte[1];
	
	public boolean isJs = false;

}
