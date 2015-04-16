package gwems;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.gwems.agents.Agent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This runs some javascript.
 * 
 * @author awootton
 *
 */
public class Js implements Runnable {

	public static Logger logger = Logger.getLogger(Js.class);

	public String js = "console.log('Hello World');";

	ScriptContext context = null;// what?

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		ScriptEngine engine = null;
		try {

			if (ec.agent.isPresent()) {
				engine = ec.global.getEngine();
				Agent agent = ec.agent.get();
				// we need to restore the context from the agent.
				// ScriptContext context = engine.getContext();
				// actually, just the engine bindings
				if (agent.bindings == null) {
					agent.bindings = engine.createBindings();
				}
				try {
					engine.eval(js, agent.bindings);// , context
				} catch (ScriptException e) {
					 logger.error("script error ",e);
				}

			} else {
				logger.error("js error: there needs to be an Agent present. ");
			}

		} catch (Exception e) {
			logger.error("js error ", e);
		} finally {
			ec.global.returnEngine(engine);
		}
	}

	public static void main(String[] args) throws JsonProcessingException {

		Publish p = new Publish();
		System.out.println(Global.serialize(p));
		System.out.println(Global.serializePretty(p));

	}

}
