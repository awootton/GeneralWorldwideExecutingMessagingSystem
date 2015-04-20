package org.gwems.js;

import java.io.FileReader;
import java.io.IOException;

import javafx.util.Pair;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class GetToKnowBindingsAndScopes {

	public static void dumpBindings(String comment , Bindings bindings) {
		System.out.println(" ------------ Dumping "+comment+" Bindings for " + bindings + " -------------------------");
		if (bindings == null)
			System.out.println("  No bindings");
		else
			for (String key : bindings.keySet())
				System.out.println("  " + key + ": " + bindings.get(key));
		System.out.println();
	}

	public static void main(String[] args) throws ScriptException {

		{
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("js");
			engine.put("a", 1);
			engine.put("b", 5);

			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			Object a = bindings.get("a");
			Object b = bindings.get("b");
			System.out.println("a = " + a);
			System.out.println("b = " + b);

			Object result = engine.eval("c = a + b;");
			System.out.println("a + b = " + result);
		}
		{
			ScriptEngineManager manager = new ScriptEngineManager();
			manager.put("global", "global bindings");
			manager.put("globalIntVar", 123456);

			dumpBindings("mgr",manager.getBindings());
			ScriptEngine engine = manager.getEngineByExtension("js");
			engine.put("engine", "engine bindings");
			engine.put("engineIntVar", 56789);

			dumpBindings("global",engine.getBindings(ScriptContext.GLOBAL_SCOPE));

			dumpBindings("engine",engine.getBindings(ScriptContext.ENGINE_SCOPE));

			try {
				Bindings bindings = engine.createBindings();
				bindings.put("engine", "overridden engine bindings");
				bindings.put("app", new GetToKnowBindingsAndScopes());
				bindings.put("bindings", bindings);
				
				dumpBindings("created",bindings);
				
				engine.eval("app.dumpBindings (bindings);", bindings);
			} catch (ScriptException se) {
				System.err.println(se.getMessage());
			}

			ScriptEngine engine2 = manager.getEngineByExtension("js");
			engine2.put("engine2", "engine2 bindings");

			dumpBindings("global2",engine2.getBindings(ScriptContext.GLOBAL_SCOPE));
			dumpBindings("engine2",engine2.getBindings(ScriptContext.ENGINE_SCOPE));
			dumpBindings("engine",engine.getBindings(ScriptContext.ENGINE_SCOPE));
		}
		
		{
	        try
	        {
	            ScriptEngine engine = 
	                new ScriptEngineManager().getEngineByName("javascript");
	                
	            for (String arg : args)
	            {
	                Bindings bindings = new SimpleBindings();
	                bindings.put("author", new Pair<String,String>("Ted","neward"));//new Person("Ted", "Neward", 39));
	                bindings.put("title", "5 Things You Didn't Know");
	                
	                FileReader fr = new FileReader(arg);
	                if (engine instanceof Compilable)
	                {
	                    System.out.println("Compiling....");// doesn't happen. 
	                    Compilable compEngine = (Compilable)engine;
	                    CompiledScript cs = compEngine.compile(fr);
	                    cs.eval(bindings);
	                }
	                else
	                    engine.eval(fr, bindings);
	            }
	        }
	        catch(IOException ioEx)
	        {
	            ioEx.printStackTrace();
	        }
	        catch(ScriptException scrEx)
	        {
	            scrEx.printStackTrace();
	        }
	    }
	}

}
