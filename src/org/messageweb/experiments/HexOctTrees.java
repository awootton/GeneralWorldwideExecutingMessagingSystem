package org.messageweb.experiments;

import javax.vecmath.Vector3d;

/**
 * Describe, precisely, power of two oct-trees with an ascii format
 * 
 * @author awootton
 *
 */
public class HexOctTrees {

	/**
	 * bias is a power of two. If bias is 0 then we return the min vector of a 1 meter cube
	 * 
	 * @param v
	 * @param bias
	 * @return
	 */
	public static String decompose(Vector3d v, int bias) {
		double power = Math.scalb(1, -bias);
		boolean xneg = v.x < 0;
		boolean yneg = v.y < 0;
		boolean zneg = v.z < 0;
		long x = (long) Math.abs(Math.floor(v.x * power));
		long y = (long) Math.abs(Math.floor(v.y * power));
		long z = (long) Math.abs(Math.floor(v.z * power));
		if (x == 0)
			xneg = false;
		if (y == 0)
			yneg = false;
		if (z == 0)
			zneg = false;
		String str = (xneg ? "-" : "") + Long.toHexString(x) + "_" + (yneg ? "-" : "") + Long.toHexString(y) + "_" + (zneg ? "-" : "") + Long.toHexString(z)
				+ "_" + bias;
		return str;
	}

	long parseSignedHexLong(String s) {
		int pos = 0;
		if (s.startsWith("-"))
			pos++;
		long val = Long.parseLong(s, 16);
		if ( pos != 0 )
			val = -val;
		return val;
	}

	public Vector3d compose(String s) {
		String[] parts = s.split("_");
		Vector3d vmin;
		if (parts.length != 4) {
			throw new NumberFormatException(s + " needs 4 _parts");
		}
		long i0, i1, i2;
		try {
			i0 = parseSignedHexLong(parts[0]);
			i1 = parseSignedHexLong(parts[1]);
			i2 = parseSignedHexLong(parts[2]);
		} catch (NumberFormatException e) {
			throw e;
		}
		int scaleFactor = Integer.parseInt(parts[3]);
		vmin = new Vector3d(i0, i1, i2);
		// how absurd do we have to get with the powers?
		double power = Math.scalb(1, scaleFactor);
		vmin.scale(power);
		return vmin;
	}

	public static void main(String[] args) {

		System.out.println(decompose(new Vector3d(0, 0, 0), 0));

		System.out.println(decompose(new Vector3d(-0.5, 0.5, -1), 0));

		System.out.println(decompose(new Vector3d(-8, 10, -20), 4));

		System.out.println(decompose(new Vector3d(-8, 17, -20), 4));

		System.out.println(decompose(new Vector3d(0, 1L << 30, 0), 10));

		System.out.println(decompose(new Vector3d(0, (1L << 30) - 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, (1L << 29) - 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, (1L << 28) - 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, (1L << 27) - 1, 0), 10));

		System.out.println(decompose(new Vector3d(0, -(1L << 30) + 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, -(1L << 29) - 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, -(1L << 28) - 1, 0), 10));
		System.out.println(decompose(new Vector3d(0, -(1L << 27) - 1, 0), 10));

	}

}
