package gwems;

/**
 * A message from a SessionAgent, across the internet, to a client. It is most useful because it's a way to find out the
 * session name of the SessionAgent. Ping.java uses one of these for a reply, It also sends the version of the server.
 * The 'server' name is for debugging.
 * 
 * @author awootton
 *
 */
public class Ack implements Runnable {

	public String session = "unknown";
	public String version = ".01";
	// warning - may be @Deprecatedd
	public String server = "S001";// the name of the org.gwems.Global

	@Override
	public void run() {
		// does nothing.
		// probably there is some javascript that executes this.
		// Java WS client may have a special case so watch for that if renaming. 
	}

}
