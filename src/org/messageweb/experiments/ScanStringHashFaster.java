package org.messageweb.experiments;

public class ScanStringHashFaster {

	static final byte[] toBase64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

	static final byte[] reverse64 = new byte[256];

	static {
		int i = 0;
		for (byte b : toBase64) {
			reverse64[((int) b) & 0xFF] = (byte) i;
			i++;
		}

	}

	static int getHash(char[] chars) {
		int h = 0;
		if (h == 0 && chars.length > 0) {
			char val[] = chars;

			for (int i = 0; i < chars.length; i++) {
				h = 31 * h + val[i];
			}
			// hash = h;
		}
		return h;
	}

	/**
	 * admin level = 24 key = BMlny hash= AAAA1BzPaZxCrW8sAVep
	 * 
	 * @param args
	 */

	public static void main(String[] args) {

		String base = "admin_";
		String tail = "AAAA";

		int mask = 0xFF;
		int offset = base.length();

		char[] chars = (base + tail).toCharArray();

		while (true) {

			int hash = getHash(chars);
			if ((hash & mask) == 0) {
				System.out.println("mask  " + Integer.toHexString(mask) + " zer " + new String(chars));
				mask *= 2;
				mask |= 1;
			}

			int i = offset;
			while (true) {
				chars[i] += 1;
				if (chars[i] > (int) 'Z') {
					chars[i] = 'A';
					i++;
					if (i >= chars.length) {
						// extend the array;
						String s = new String(chars);
						s += "A";
						chars = s.toCharArray();
						break;
					}
				} else {
					break;
				}
			}
		}
	}
}
