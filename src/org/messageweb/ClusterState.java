package org.messageweb;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.regions.Regions;

public class ClusterState {

	String name = "" + Regions.US_WEST_2;

	Map<String, ServerGlobalState> name2server = new HashMap<>();

}
