package org.gwems.servers.impl;

import gwems.Js;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.gwems.servers.Global;

public class JsEnginePool {

	List<ScriptEngine> engines = Collections.synchronizedList(new LinkedList<>());
	Global global;

	public JsEnginePool(Global global) {
		super();
		this.global = global;
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

	private ScriptEngine create() {

		ScriptEngineManager factory = new ScriptEngineManager();
		// create JavaScript engine
		ScriptEngine engine = factory.getEngineByName("JavaScript");//
		// ScriptEngine engine = factory.getEngineByName("nashorn");// they are the same
		// init some interfaces.

		engine.put("console", new JsConsole());
		engine.put("global", new GlobalInterface(global));

		return engine;
	}

	public static class JsConsole {
		public void log(String s) {
			Js.logger.info(s);
			System.out.println("JsConsole:" + s);
		}
	}
	
	public static class GlobalInterface {
		Global global;
		
		public GlobalInterface(Global global) {
			super();
			this.global = global;
		}

		public String getId(){
			return global.id;
		}
	}

	public void giveBack(ScriptEngine engine) {
		engines.add(engine);
	}

}
