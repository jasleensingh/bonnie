package edu.ucla.bonnie.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	public static boolean accept(String pathname) {
		final String[] exts = { "mp3", "wav", "wma" };
		int ext_idx = pathname.lastIndexOf(".");
		if (ext_idx > 0 && ext_idx < pathname.length() - 1) {
			String ext = pathname.substring(ext_idx + 1);
			for (String e : exts) {
				if (e.equalsIgnoreCase(ext)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String randomAlphaNum(int length) {
		byte[] buf = new byte[length];
		for (int i = 0; i < buf.length; i++) {
			int n = (int) (Math.random() * 62);
			if (n < 10) {
				buf[i] = (byte) (0x30 + n);
			} else if (n < 36) {
				n -= 10;
				buf[i] = (byte) (0x41 + n);
			} else {
				n -= 36;
				buf[i] = (byte) (0x61 + n);
			}
		}
		return new String(buf);
	}

	public static void readFully(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		out.flush();
	}
}
