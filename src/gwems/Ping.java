package gwems;

import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A client sends one of these to a server. The server creates an Ack message and sends it back.
 * 
 * {"@":"gwems.Ping"}
 * 
 * @author awootton
 *
 */
public class Ping implements Runnable {

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent agent = (SessionAgent) ec.agent.get();
			Ack ack = new Ack();
			ack.session = agent.getKey();
			ack.server = ec.global.id;
			agent.writeAndFlush(ack);
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Ping()));
	}
}
