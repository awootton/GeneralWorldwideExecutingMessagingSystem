package org.gwems.servers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public  class Util {
	
	public static final int twoMinutes = 120 * 1000;
	public static final int fifteenMinutes = 15 * 60 * 1000;

	public static String serialize( Object that)  {
		try {
			return Global.serialize(that);
		} catch (JsonProcessingException e) {
		 
		}
		return "fail";
	}
	
	public static Object deserialize(String src)  {
		try {
			return Global.deserialize(src);
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
}
