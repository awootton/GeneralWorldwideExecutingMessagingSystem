package org.gwems.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.script.Bindings;

import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;
import org.gwems.util.AgentRunnablesQueue;

/**
 * 
 * needs: time to live. helpers. test with storage. We'll want to save these in db soon. maybe.
 * 
 * @author awootton
 *
 */

public abstract class Agent implements Comparable<Agent> {

	/**
	 * We'll need to install the messageQ manually. Fortunately there will only be a handful of places where these are
	 * deserialized. Or, none.
	 */
	public AgentRunnablesQueue messageQ = null;// protected?

	private final String key;// must be unique!
	public final Map<Object, Object> userMap;
	public final Global global;
	
	public Bindings bindings = null; // needs accessors

	public Agent(Global global, String key) {
		this.global = global;
		this.key = key;
		this.userMap = new HashMap<>();
		// agents always subscribe to their own key so that they can make a SASE
		global.subscribe(this, key);
		// note that the agent timeout unsubs the agent
	}

	@Override
	public String toString() {
		return this.getClass() + ":" + key;
	}

	public final String getKey() {
		return key;
	}

	/**
	 * Sometimes the agent might want to filter the messages. To do that override this. Otherwise, as you can see, the
	 * messages just run.
	 * 
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
