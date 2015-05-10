package gwems;

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
 * {"@":"gwems.Js","js":"console.log('Hello World');"}
 * 
 * 
 * @author awootton
 *
 */
public class Js implements Runnable {

	public static Logger logger = Logger.getLogger(Js.class);

	public String js = "console.log('Hello World');";

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
					// this won't really work in the long run.
					engine.eval("var java = {};", agent.bindings);
				}
				try {
					ec.isJs = true;
					engine.eval(js, agent.bindings);
					ec.isJs = false;
					// debug much?
					// Bindings globalBinding = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
					// Bindings engineBinding = engine.getBindings(ScriptContext.ENGINE_SCOPE);
					// GetToKnowBindingsAndScopes.dumpBindings("global",globalBinding);
					// GetToKnowBindingsAndScopes.dumpBindings("engine",engineBinding);
					// GetToKnowBindingsAndScopes.dumpBindings("agent",agent.bindings);

				} catch (ScriptException e) {
					logger.error("script error ", e);
				} finally {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((js == null) ? 0 : js.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Js other = (Js) obj;
		if (js == null) {
			if (other.js != null)
				return false;
		} else if (!js.equals(other.js))
			return false;
		return true;
	}

	public static void main(String[] args) throws JsonProcessingException {

		Js p = new Js();
		System.out.println(Global.serialize(p));
		System.out.println(Global.serializePretty(p));

	}

}
