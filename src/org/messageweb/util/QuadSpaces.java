package org.messageweb.util;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

import javax.vecmath.Vector3d;
import javax.websocket.EncodeException;

import org.junit.Assert;

public class QuadSpaces {

	/**
	 * Some math test's I'm going to have to write in js. I'm just making notes here. The GWEMS is not likely to have
	 * any 3d math at all.
	 * 
	 * We need to enumerate 3d space into quad trees (not oct trees or tri trees?). In absolute coordinates.
	 * 
	 * We would like to have a string format to subscribe to like: 1_2_-1_6 or x_y_z_p where x_y_z have to have values
	 * [-2..1] and p is an even integer that is interpreted as a power of 2. I need to put them in url's.
	 * 
	 * what are they called ? Quadspaces
	 *
	 * 0_0_0_0 is the minimum corner of the zero meter cube. 0_0_0_1 is an error. 0_0_0_2 is a 4 by 4 by 4 meter cube at
	 * the origin. .
	 * 
	 */

	/**
	 * Return the min and max vertices for the quadspace described by the string.
	 * 
	 * @param s
	 * @return
	 * @throws EncodeException
	 *             if the format is broken.
	 */
	public static Pair<Vector3d, Vector3d> getMinMax(String s) throws EncodeException {
		String[] parts = s.split("_");
		Vector3d vmin;
		Vector3d vmax;
		if (parts.length != 4) {
			throw new NumberFormatException(s + " needs 4 _parts");
		}
		int i0, i1, i2;
		try {
			i0 = Integer.parseInt(parts[0]);
			i1 = Integer.parseInt(parts[1]);
			i2 = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e) {
			throw e;
		}
		if ((((i0 + 2) & 0xFFFFFFFC) != 0) || (((i1 + 2) & 0xFFFFFFFC) != 0) || (((i2 + 2) & 0xFFFFFFFC) != 0)) {
			throw new NumberFormatException(s + " must be signed 2 bit");
		}
		int scaleFactor = Integer.parseInt(parts[3]);
		vmin = new Vector3d(i0, i1, i2);
		vmax = new Vector3d(i0 + 1, i1 + 1, i2 + 1);
		// how absurd do we have to get with the powers?
		double power = Math.scalb(1, scaleFactor);
		vmin.scale(power);
		vmax.scale(power);
		return new Pair<Vector3d, Vector3d>(vmin, vmax);
	}

	/** Return the vector of the corner of the cube described by s
	 * where is is of the form x_y_x_p and x, y, and z MUST be between -2 and 1
	 * and p MUST be an even number. 
	 * p is a power of 2
	 * 
	 * @param s
	 * @return
	 * @throws EncodeException
	 */
	public static Vector3d getMinCorner(String s) throws EncodeException {
		String[] parts = s.split("_");
		Vector3d vmin;
		if (parts.length != 4) {
			throw new NumberFormatException(s + " needs 4 _parts");
		}
		int i0, i1, i2;
		try {
			i0 = Integer.parseInt(parts[0]);
			i1 = Integer.parseInt(parts[1]);
			i2 = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e) {
			throw e;
		}
		int off = 2;
		if ((((i0 + off) & 0xFFFFFFFC) != 0) || (((i1 + off) & 0xFFFFFFFC) != 0) || (((i2 + off) & 0xFFFFFFFC) != 0)) {
			throw new NumberFormatException(s + " must be signed 2 bit");
		}
		int scaleFactor = Integer.parseInt(parts[3]);
		vmin = new Vector3d(i0, i1, i2);
		// how absurd do we have to get with the powers?
		double power = Math.scalb(1, scaleFactor);
		vmin.scale(power);
		return vmin;
	}

	/** The complete opposite of decompose. 
	 * A list of quadspaces can be added to recover a vector in 3 space. 
	 * 
	 * @throws EncodeException
	 * 
	 */
	public static Vector3d reconstitute(List<String> quadspaces) throws EncodeException {
		Vector3d sum = new Vector3d();
		for (String string : quadspaces) {
			sum.add(getMinCorner(string));
		}
		return sum;
	}

	/**
	 * Return a list of quads, low quads first, that contain this vector. large quads that start with 0_0_0 are
	 * suppressed.
	 * 
	 * fractional quads, less than 1 meter, are left out unless bias is < 0. Bias must be even! It's a power of 2. We
	 * 
	 * @param v
	 * @return
	 */
	public static List<String> decompose(Vector3d v, int bias) {
		assert ((bias & 1) == 0) : "bias must be even";
		List<String> result = new ArrayList<>();
		double power = Math.scalb(1, -bias);
		long x = (long) (v.x * power);
		long y = (long) (v.y * power);
		long z = (long) (v.z * power);
		int p = bias;
		int i1, i2, i3;
		while (true) {
			// always do at least one?
			x += 2;
			y += 2;
			z += 2;
			i1 = (int) ((x) & 3);
			i2 = (int) ((y) & 3);
			i3 = (int) ((z) & 3);
			String s = "" + (i1 - 2) + "_" + (i2 - 2) + "_" + (i3 - 2) + "_" + (p);
			x -= i1;
			y -= i2;
			z -= i3;
			x >>= 2;
			y >>= 2;
			z >>= 2;
			p += 2;
			// we could suppress zero fraction parts here
			// if ( s.startsWith("0_0_0_"))
			result.add(s);
			if (x == 0 && y == 0 && z == 0) {
				break;
			}
		}
		return result;
	}

	public static void main(String[] args) throws EncodeException {

		List<String> list;

		list = decompose(new Vector3d(0, 0, 0), 0);
		Assert.assertEquals("[0_0_0_0]", "" + list);

		list = decompose(new Vector3d(1, 1, 1), 0);
		Assert.assertEquals("[1_1_1_0]", "" + list);

		// note that it's forward 4 and back 2
		list = decompose(new Vector3d(2, 2, 2), 0);
		Assert.assertEquals("[-2_-2_-2_0, 1_1_1_2]", "" + list);

		// note that it's forward 4 and back 1
		list = decompose(new Vector3d(3, 3, 3), 0);
		Assert.assertEquals("[-1_-1_-1_0, 1_1_1_2]", "" + list);

		// forwrd 4 back 0
		list = decompose(new Vector3d(4, 4, 4), 0);
		Assert.assertEquals("[0_0_0_0, 1_1_1_2]", "" + list);

		list = decompose(new Vector3d(-1, -1, -1), 0);
		Assert.assertEquals("[-1_-1_-1_0]", "" + list);

		list = decompose(new Vector3d(-4, -5, -6), 0);
		Assert.assertEquals("[0_-1_-2_0, -1_-1_-1_2]", "" + list);

		list = decompose(new Vector3d(-44, 123456, 11), 0);
		Assert.assertEquals("[0_0_-1_0, 1_0_-1_2, 1_0_1_4, -1_1_0_6, 0_-2_0_8, 0_1_0_10, 0_-2_0_12, 0_0_0_14, 0_-2_0_16, 0_1_0_18]", "" + list);

		// the reverse should add up
		Vector3d input = new Vector3d(-44, 123456, 11);
		list = decompose(input, 0);
		Vector3d re = reconstitute(list);
		Assert.assertEquals(re, input);

		// try fractions

		// it rounds
		list = decompose(new Vector3d(0, 0.25, 0), 0);
		Assert.assertEquals("[0_0_0_0]", "" + list);

		input = new Vector3d(-44 / 256.0, 123456 / 256.0, 11 / 256.0);
		// unless we put the rounding level down 2^-12
		list = decompose(input, -12);
		re = reconstitute(list);
		Assert.assertEquals(re, input);

		System.out.println(getMinMax("0_0_0_0"));

		System.out.println(getMinMax("1_1_0_0"));
		System.out.println(getMinMax("0_0_0_2"));

		System.out.println(getMinMax("-2_-2_-2_2"));

		System.out.println(getMinMax("-2_-1_-0_1"));
		System.out.println(getMinMax("-2_-1_0_1"));

		System.out.println(getMinMax("1_1_1_32"));

	}
}
