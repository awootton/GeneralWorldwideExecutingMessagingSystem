package org.gwems.common.core;

import java.io.FileNotFoundException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JavaScriptTests {

	static String baseDir = "WebContent/";

	public static Object THREE;

	public static Invocable inv;
	public static Object lod;
	public static ScriptEngine engine;

	@BeforeClass
	public static void setup() throws ScriptException, FileNotFoundException {

		ScriptEngineManager factory = new ScriptEngineManager();
		// create JavaScript engine
		engine = factory.getEngineByName("JavaScript");//
		// ScriptEngine engine = factory.getEngineByName("nashorn");// they are the same

		// fails. why? engine.eval(new java.io.FileReader( baseDir + "js/three.js" ));
		engine.eval("var THREE = { REVISION: '71' };");
		engine.eval(new java.io.FileReader(baseDir + "js/Vector3.js"));

		inv = (Invocable) engine;
		// get script object on which we want to call the method
		THREE = engine.get("THREE");
		engine.eval(new java.io.FileReader(baseDir + "js/QuadSpaces.js"));
		lod = engine.get("QuadSpaces");
	}

	@AfterClass
	public static void closeAll() {
	}

}
