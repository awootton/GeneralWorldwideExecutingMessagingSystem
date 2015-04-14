package org.gwems.js;

import java.io.FileNotFoundException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


// This is just experimental crap

public class RunJS {
	
	static String baseDir = "WebContent/";
	
	
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		
	       // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");//
        //ScriptEngine engine = factory.getEngineByName("nashorn");// they are the same 
        // evaluate JavaScript code from given file - specified by first argument
       
        // fails. why? engine.eval(new java.io.FileReader( baseDir + "js/three.js" ));
        engine.eval("var THREE = { REVISION: '71' };");
        engine.eval(new java.io.FileReader( baseDir + "js/Vector3.js" ));
        
         
        // we could ;  engine.put("file", f);
        
        Invocable inv = (Invocable) engine;

        // get script object on which we want to call the method
       // Object THREE = engine.get("THREE");
        
        //System.out.println(THREE);
        
        engine.eval(new java.io.FileReader( baseDir + "js/QuadSpaces.js" ));
	
        try {
        	Object lod = engine.get("LodMath");
        	System.out.println(lod);
			Object lodlist = inv.invokeMethod(lod,"decompose", "a");
			
			Object s = inv.invokeMethod(lod,"stringList", lodlist);
			
			System.out.println(s);
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
        
	}

}
