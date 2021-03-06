package org.gwems.util;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.script.ScriptException;
import javax.vecmath.Vector3d;

import org.gwems.common.core.JavaScriptTests;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.experiments.QuadSpaces;

public class QuadSpacesJStests extends JavaScriptTests {

	public String lsDecompose(Vector3d v, int bias) throws NoSuchMethodException, ScriptException {
	
		Object ov = inv.invokeMethod(lod, "vector", v.x, v.y, v.z);

		Object lodlist = inv.invokeMethod(lod, "decompose", ov, bias);

		Object s = inv.invokeMethod(lod, "listToString", lodlist);
		return "" + s;
	}

	public Object lsDecompose2list(Vector3d v, int bias) throws NoSuchMethodException, ScriptException {
		Object ov = inv.invokeMethod(lod, "vector", v.x, v.y, v.z);

		Object lodlist = inv.invokeMethod(lod, "decompose", ov, bias);

		return lodlist;
	}

	public Vector3d toVector(Object vect) throws NoSuchMethodException, ScriptException {

		Double x = (Double) inv.invokeMethod(lod, "getX", vect);
		Double y = (Double) inv.invokeMethod(lod, "getY", vect);
		Double z = (Double) inv.invokeMethod(lod, "getZ", vect);

		return new Vector3d(x, y, z);
	}

	public Vector3d lsgetMinCorner(String str) throws NoSuchMethodException, ScriptException {

		Object vector = inv.invokeMethod(lod, "getMinCorner", str);
		// how do I get the parts?

		Vector3d got = toVector(vector);

		return got;
	}

	public Vector3d reconstitute(Object strList) throws NoSuchMethodException, ScriptException {

		Object vector = inv.invokeMethod(lod, "reconstitute", strList);
		// how do I get the parts?

		Vector3d got = toVector(vector);

		return got;
	}

	String doBoth(Vector3d v, int bias) throws NoSuchMethodException, ScriptException {

		List<String> slist = QuadSpaces.decompose(v, bias);
		Object str = lsDecompose(v, bias);
		
		Assert.assertEquals("" + slist, str);
		
		System.out.println(" mn " + lsgetMinCorner(slist.get(0)));

		Object strList = lsDecompose2list(v, bias);
		Vector3d redone = reconstitute(strList);

		Assert.assertEquals(v, redone);
		
		//surroundingSpaceNames(Vector3d position, int start, int end) {
		
		Set<String>  surrounds = QuadSpaces.surroundingSpaceNames(v, 0, 3);
		String surrstr = "" + new TreeSet<String>(surrounds);
		System.out.println(surrstr);

		return "" + slist;
	}

	@Test
	public void t1() throws NoSuchMethodException, ScriptException {

		Vector3d v = new Vector3d(1, 2, 3);
		doBoth(v, 0);
		v = new Vector3d(-44, 123456, 11);
		doBoth(v, 0);

		v = new Vector3d(-44 / 256.0, 123456 / 256.0, 11 / 256.0);
		doBoth(v, -11);
		
		v = new Vector3d(-4, 12, 88);
		doBoth(v, 2);

	}

	public static void main(String[] args) throws FileNotFoundException, ScriptException, NoSuchMethodException {
		QuadSpacesJStests test = new QuadSpacesJStests();
		JavaScriptTests.setup();
		test.t1();
		
		System.out.println("done");
	}
}
