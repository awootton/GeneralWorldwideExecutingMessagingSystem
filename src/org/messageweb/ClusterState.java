package org.messageweb;

import org.apache.log4j.Logger;

public class ClusterState {
	
	public static Logger logger = Logger.getLogger(ClusterState.class);

	String name = "" + "USW2";//Regions.US_WEST_2;
	
	public int redis_port = 6380;// 6379;
	public String redis_server = "localhost";
	
	

//	Map<String, Global> name2server = new HashMap<>();
//	
//	// it's needs to have some (any!?) entry points for other regions.
//	// it's ok to go through the load-balancers to get there.
//	// maybe we should just hard code them here.
//	
//	Set<String> regionUrls = new TreeSet<>();
//	
//	public ClusterState() {
//		regionUrls.clear();
//		regionUrls.add("localhost:8081");
//		regionUrls.add("localhost:8091");
//		regionUrls.add("localhost:8101");
//	}
//	
//	/** The main point is that each region will have it's own redis cluster.
//	 *  This must be modeled in the sim. 
//	 *  
//	 *  redis is usually on 6379.  we can use 6380
//	 */

}
