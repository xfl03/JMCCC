package org.to2mbn.jmccc.mcdownloader.download.util;

import java.io.File;
import java.io.IOException;

public final class FileUtils {

	public static void mkdirs(File dir) throws IOException {
		if (!dir.mkdirs()) {
			throw new IOException("Cannot mkdirs: " + dir);
		}
	}
	
	public static void testRead(File file) throws IOException {
		if (!file.canRead()) {
			throw new IOException("Cannot read from: " + file);
		}
	}

	public static void testWrite(File file) throws IOException {
		if (!file.canWrite()) {
			throw new IOException("Cannot write to: " + file);
		}
	}

	public static void prepareWrite(File file) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			mkdirs(parent);
		}
	}
	
	private FileUtils() {
	}
}
