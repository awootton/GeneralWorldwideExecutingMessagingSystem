package org.gwems.servers;

import org.apache.log4j.Logger;

public class ClusterState {
	
	public static Logger logger = Logger.getLogger(ClusterState.class);

	String name = "USW2";//aka Regions.US_WEST_2;
	
	public int redis_port = 6379;
	public String redis_server = "localhost";
	
	public int super_redis_port = 6381;
	public String super_redis_server = "localhost";

}
