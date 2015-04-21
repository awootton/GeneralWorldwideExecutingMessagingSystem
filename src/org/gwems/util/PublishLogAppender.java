package org.gwems.util;

import gwems.Push2Client;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/** This enables ALL log messages that have an agent context, with an id, to publish to 
 * a specific channel that can be followed.
 * I think the level is set at DEBUG but not trace.
 * 
 * TODO: api for people to change their level.
 * 
 * @author awootton
 *
 */
public class PublishLogAppender extends AppenderSkeleton {
	
	public static final boolean publishAgentLogs = false;

	@SuppressWarnings("unused")
	@Override
	protected void append(LoggingEvent event) {

		ExecutionContext ec = Global.getContext();
		if (publishAgentLogs && ec.agent.isPresent()) {
			String message = event.getRenderedMessage();
			String channel = ec.agent.get().getKey() + "#log";
			ec.global.publishLog(channel, new Push2Client(message));
		}
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return false;
	}

}
