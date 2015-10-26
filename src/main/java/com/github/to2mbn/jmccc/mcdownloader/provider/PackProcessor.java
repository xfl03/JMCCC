package com.github.to2mbn.jmccc.mcdownloader.provider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;
import com.github.to2mbn.jmccc.mcdownloader.download.ResultProcessor;

public class PackProcessor implements ResultProcessor<byte[], Object> {

	private static final byte[] POSTFIX;

	static {
		try {
			POSTFIX = "SIGN".getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("unable to encode ascii", e);
		}
	}

	private Unpacker unpacker = Pack200.newUnpacker();
	private File target;

	public PackProcessor(File target) {
		this.target = target;
	}

	@Override
	public Object process(byte[] data) throws IOException {
		// create parent dir
		File parent = target.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		if (data.length < 8) {
			throw new IOException("pack data too short: " + data.length);
		}
		for (int i = 0; i < 4; i++) {
			if (data[data.length - i] != POSTFIX[POSTFIX.length - i]) {
				throw new IOException("bad postfix");
			}
		}
		int checksumsLength = data[(data.length - 8)] & 0xff | (data[(data.length - 7)] & 0xff) << 8 | (data[(data.length - 6)] & 0xff) << 16 | (data[(data.length - 5)] & 0xff) << 24;
		if (checksumsLength < 0 || checksumsLength > data.length - 8) {
			throw new IOException("illegal checksums length: " + checksumsLength);
		}
		JarEntry checksumsEntry = new JarEntry("checksums.sha1");
		checksumsEntry.setTime(0);

		try (JarOutputStream out = new JarOutputStream(new FileOutputStream(target))) {
			unpacker.unpack(new ByteArrayInputStream(data), out);
			out.putNextEntry(checksumsEntry);
			out.write(data, data.length - 8 - checksumsLength, checksumsLength);
			out.closeEntry();
		}
		return null;
	}

}
