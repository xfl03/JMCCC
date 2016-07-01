package org.to2mbn.jmccc.mojangapi.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import org.to2mbn.jmccc.util.HexUtils;

public class MultipartBuilder {

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final int READY = 0;
	private static final int DISPOSITION = 1;
	private static final int HEADERS = 2;
	private static final int FINISHED = 3;

	private static String randomBoundary() {
		byte[] rnd = new byte[8];
		RANDOM.nextBytes(rnd);
		return "---------------------------" + HexUtils.bytesToHex(rnd);
	}

	private String boundary = randomBoundary();
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
	private int status = READY;

	public String getBoundary() {
		return boundary;
	}

	public String getContentType() {
		return "multipart/form-data; boundary=" + boundary;
	}

	public MultipartBuilder disposition(String key, String value) throws IOException {
		if (status != READY && status != DISPOSITION) {
			throw new IllegalStateException("The current status is " + status + ", expected: " + READY + " or " + DISPOSITION);
		}

		if (status == READY) {
			status = DISPOSITION;
			writer.write("--");
			writer.write(boundary);
			writer.write("\r\nContent-Disposition: form-data");
		}

		writer.write("; ");
		writer.write(key);
		writer.write("=\"");
		writer.write(value);
		writer.write("\"");

		return this;
	}

	public MultipartBuilder header(String key, String value) throws IOException {
		if (status != DISPOSITION && status != HEADERS) {
			throw new IllegalStateException("The current status is " + status + ", expected: " + DISPOSITION + " or " + HEADERS);
		}

		if (status == DISPOSITION) {
			status = HEADERS;
			writer.write("\r\n");
		}

		writer.write(key);
		writer.write(": ");
		writer.write(value);
		writer.write("\r\n");

		return this;
	}

	public MultipartBuilder content(byte[] payload) throws IOException {
		if (status != DISPOSITION && status != HEADERS) {
			throw new IllegalStateException("The current status is " + status + ", expected: " + DISPOSITION + " or " + HEADERS);
		}

		if (status == DISPOSITION) {
			writer.write("\r\n");
		}
		status = READY;

		writer.write("\r\n");
		writer.flush();
		out.write(payload);
		writer.write("\r\n");

		return this;
	}

	public byte[] finish() throws IOException {
		if (status != READY) {
			throw new IllegalStateException("The current status is " + status + ", expected: " + READY);
		}
		status = FINISHED;

		writer.write("--");
		writer.write(boundary);
		writer.write("--\r\n");
		writer.flush();
		return out.toByteArray();
	}

}
