package gwems;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.apache.log4j.Logger;
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

			engine = ec.global.getEngine();
			engine.eval(js);// , context

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
