package org.messageweb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.amazonaws.regions.Regions;

public class ClusterState {
	
	public static Logger logger = Logger.getLogger(ClusterState.class);

	String name = "" + Regions.US_WEST_2;

	Map<String, ServerGlobalState> name2server = new HashMap<>();
	
	// it's needs to have some (any!?) entry points for other regions.
	// it's ok to go through the load-balancers to get there.
	// maybe we should just hard code them here.
	
	Set<String> regionUrls = new TreeSet<>();
	
	public ClusterState() {
		regionUrls.clear();
		regionUrls.add("localhost:8081");
		regionUrls.add("localhost:8091");
		regionUrls.add("localhost:8101");
	}
	
	/** The main point is that each region will have it's own redis cluster.
	 *  This must be modeled in the sim. 
	 *  
	 *  redis is usually on 6379.  we can use 6389 and 6399
	 */

}
