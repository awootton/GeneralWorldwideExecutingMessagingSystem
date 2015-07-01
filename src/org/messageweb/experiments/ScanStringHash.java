package org.messageweb.experiments;


public class ScanStringHash {

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

	/**
	 * admin level = 24 key = BMlny hash= AAAA1BzPaZxCrW8sAVep
	 * 
	 * @param args
	 */

	public static void main(String[] args) {

		String base = "admin_";
		String tail = "AAAA";
		byte[] keyBytes = tail.getBytes();

		int mask = 0xFF;
		
		while (true) {

			String target = base + tail;
			if ((target.hashCode() & mask ) == 0) {
				System.out.println("mask  " + Integer.toHexString(mask) + " zer " +  target);
				mask *= 2;
				mask |= 1;
			}

			int i;
			for (i = keyBytes.length - 1; i >= 0; i--) {
				byte b = reverse64[((int) keyBytes[i]) & 0xFF];
				b++;
				if ((int) b >= 64) {
					b = 0;// carry
				}
				b = toBase64[((int) b) & 0xFF];
				keyBytes[i] = b;
				if (b != 'A')
					break;
			}
			if (i == 0) {
				keyBytes = ("B" + new String(keyBytes)).getBytes();
			}
			tail = new String(keyBytes);
		}
	}
}
