package org.to2mbn.jmccc.util;

import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class IOUtils {

	public static JSONObject toJson(File file) throws JSONException, IOException {
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8")) {
			return new JSONObject(new JSONTokener(reader));
		}
	}

	public static JSONObject toJson(byte[] data) throws JSONException {
		return new JSONObject(toString(data));
	}

	public static String toString(File file) throws IOException {
		CharArrayWriter w = new CharArrayWriter((int) (file.length() / 2));
		try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			char[] buf = new char[4096]; // 8192 bytes
			int read;
			while ((read = reader.read(buf)) != -1) {
				w.write(buf, 0, read);
			}
		}
		return new String(w.toCharArray());
	}

	public static String toString(byte[] data) {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 is not supported", e);
		}
	}

	private IOUtils() {}

}
