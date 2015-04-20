package org.gwems.servers;

import org.apache.log4j.Logger;
import org.gwemdis.GwemsPubSub;
import org.gwems.util.PubSub;

/**
 * A reference to a pub sub for one server. It can be shared by all the servers that use the same pub sub.
 * 
 * @author awootton
 *
 */
public class ClusterState {

	public static Logger logger = Logger.getLogger(ClusterState.class);

	String name = "USW2";// aka Regions.US_WEST_2;

	public int redis_port = 6379;
	public String redis_server = "localhost";

	// /public int super_redis_port = 6381;
	// /public String super_redis_server = "localhost";

	/**
	 * TODO: get rid of redis.
	 * 
	 * @param handler
	 * @param id
	 * @return
	 */
	public PubSub pubSubFactory(PubSub.Handler handler, String id) {
		// PubSub thePubSub = new JedisRedisPubSubImpl(this.redis_server, this.redis_port, handler, id);

		PubSub thePubSub = null;
		if (redis_server != null) {
			thePubSub = new GwemsPubSub(this.redis_server, this.redis_port, handler, id);
		}

		return thePubSub;
	}
}
