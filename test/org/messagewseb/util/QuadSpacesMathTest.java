package org.messagewseb.util;

import java.util.List;

import javax.vecmath.Vector3d;
import javax.websocket.EncodeException;

import org.junit.Assert;
import org.junit.Test;
import org.messageweb.util.QuadSpaces;

public class QuadSpacesMathTest {
	
	
	@Test
	public void t1() throws EncodeException {
		
		List<String> list;

		list = QuadSpaces.decompose(new Vector3d(0, 0, 0), 0);
		Assert.assertEquals("[0_0_0_0]", "" + list);

		list = QuadSpaces.decompose(new Vector3d(1, 1, 1), 0);
		Assert.assertEquals("[1_1_1_0]", "" + list);

		// note that it's forward 4 and back 2
		list = QuadSpaces.decompose(new Vector3d(2, 2, 2), 0);
		Assert.assertEquals("[-2_-2_-2_0, 1_1_1_2]", "" + list);

		// note that it's forward 4 and back 1
		list = QuadSpaces.decompose(new Vector3d(3, 3, 3), 0);
		Assert.assertEquals("[-1_-1_-1_0, 1_1_1_2]", "" + list);

		// forwrd 4 back 0
		list = QuadSpaces.decompose(new Vector3d(4, 4, 4), 0);
		Assert.assertEquals("[0_0_0_0, 1_1_1_2]", "" + list);

		list = QuadSpaces.decompose(new Vector3d(-1, -1, -1), 0);
		Assert.assertEquals("[-1_-1_-1_0]", "" + list);

		list = QuadSpaces.decompose(new Vector3d(-4, -5, -6), 0);
		Assert.assertEquals("[0_-1_-2_0, -1_-1_-1_2]", "" + list);

		list = QuadSpaces.decompose(new Vector3d(-44, 123456, 11), 0);
		Assert.assertEquals("[0_0_-1_0, 1_0_-1_2, 1_0_1_4, -1_1_0_6, 0_-2_0_8, 0_1_0_10, 0_-2_0_12, 0_0_0_14, 0_-2_0_16, 0_1_0_18]", "" + list);

		// the reverse should add up
		Vector3d input = new Vector3d(-44, 123456, 11);
		list = QuadSpaces.decompose(input, 0);
		Vector3d re = QuadSpaces.reconstitute(list);
		Assert.assertEquals(re, input);

		// try fractions

		// it rounds
		list = QuadSpaces.decompose(new Vector3d(0, 0.25, 0), 0);
		Assert.assertEquals("[0_0_0_0]", "" + list);

		input = new Vector3d(-44 / 256.0, 123456 / 256.0, 11 / 256.0);
		// unless we put the rounding level down 2^-12
		list = QuadSpaces.decompose(input, -12);
		re = QuadSpaces.reconstitute(list);
		Assert.assertEquals(re, input);


	}

}
