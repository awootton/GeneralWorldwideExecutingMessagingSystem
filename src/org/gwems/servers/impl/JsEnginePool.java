package org.gwems.servers.impl;

import gwems.Js;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;

public class JsEnginePool {

	public static Logger logger = Logger.getLogger(JsEnginePool.class);

	List<ScriptEngine> engines = Collections.synchronizedList(new LinkedList<>());
	Global global;

	ScriptEngineManager factory;

	public JsEnginePool(Global global) {
		super();
		this.global = global;

		factory = new ScriptEngineManager();

		factory.put("console", new JsConsole());
		factory.put("global", new GlobalInterface(global));
		// anything else?

		ScriptEngine engine = factory.getEngineByName("JavaScript");//
		try {
			engine.eval("var java = {};");
			Bindings globalBinding = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
			Bindings engineBinding = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			Object obj = engineBinding.get("java");
			// why does this not work?
			globalBinding.put("java", obj);

		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public ScriptEngine get() {
		ScriptEngine res = null;
		if (engines.size() == 0) {
			res = create();
			return res;
		}
		res = engines.get(engines.size() - 1);
		return res;
	}

	private int count = 0;

	public int getCreatedCount() {
		return count;
	}

	private ScriptEngine create() {
		// create JavaScript engine
		ScriptEngine engine = factory.getEngineByName("JavaScript");//
		// ScriptEngine engine = factory.getEngineByName("nashorn");// they are the same
		logger.info("created new JavaScript engine #" + count++);
		// bindings made here don't stick because
		// a new engine bindings is set by every agent
		return engine;
	}

	public static class JsConsole {
		public void log(String s) {
			Js.logger.debug(s);
			System.out.println("JsConsole:" + s);
		}
	}

	public static class GlobalInterface {
		Global global;

		public GlobalInterface(Global global) {
			super();
			this.global = global;
		}

		public String getId() {
			return global.id;
		}
	}

	public void giveBack(ScriptEngine engine) {
		engines.add(engine);
	}

}
