package org.messageweb.experiments;

import java.security.MessageDigest;
import java.util.Base64;

import org.gwems.servers.ExecutionContext;

public class ShaTests {

	static int calcLevel(byte[] bytes) {
		int byteval = 0;
		int bitval = 0;

		while (bytes[byteval] == 0 && byteval < bytes.length) {
			byteval++;
		}
		int tmp = (int) bytes[byteval];

		int mask = 0x80;
		while (((tmp & mask) == 0) && (mask != 0)) {
			mask >>= 1;
			bitval++;
		}
		return byteval * 8 + bitval;
	}

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

		ExecutionContext ec = new ExecutionContext();

		String user = "F3nDnoQ";

		String key = "ACxa";

		// let's 'adjust' the key
		byte[] keyBytes = key.getBytes(ec.utf8);
		for (int i = 0; i < keyBytes.length; i++) {
			byte b = keyBytes[i];
			// map everything that is not base54 to A
			b = reverse64[((int) b) & 0xFF];
			b = toBase64[((int) b) & 0xFF];
			keyBytes[i] = b;
		}
		key = new String(keyBytes, ec.utf8);

		MessageDigest md = ec.sha256;

		byte[] userBytes = user.getBytes(ec.utf8);

		keyBytes = key.getBytes(ec.utf8);

		int level = 10;

		{// a test
			md.update(userBytes);
			md.update(keyBytes);
			byte[] bytes = md.digest();
			System.out.println(Base64.getEncoder().encodeToString(bytes));// ESWOwmDeTs
			md.update(bytes);
			bytes = md.digest();
			String str = Base64.getEncoder().encodeToString(bytes);
			System.out.println("user = " + user + " key = " + new String(keyBytes) + " hash= " + str);
		}// user = admin key = BCFSzAA hash= nQROny8RQYim9XFKj4EYqUMTXoA8OWUYL1M61MpFzB0=

		long startTime = System.currentTimeMillis();

		while (true) {

			md.update(userBytes);
			md.update(keyBytes);
			byte[] bytes = md.digest();
			md.update(bytes);
			bytes = md.digest();

			int tmp = calcLevel(bytes);
			if (tmp > level) {
				level = tmp;
				String str = Base64.getEncoder().encodeToString(bytes);
				String shorter = str.substring(0, 25);
				long now = System.currentTimeMillis();
				System.out.println("user = " + user + " level = " + level + " key = " + new String(keyBytes) + " hash= " + shorter + " time="
						+ (now - startTime)/1000);
			}
			// increment the key
			// here's just one way
			// assume that the bytes are inside of base64;
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
		}
	}

}
