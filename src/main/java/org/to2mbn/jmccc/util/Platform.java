package org.to2mbn.jmccc.util;

import java.nio.charset.Charset;

public enum Platform {
	WINDOWS, LINUX, OSX, UNKNOWN;

	/**
	 * Returns the file separator on the current platform.
	 * <p>
	 * This method refers to <code>System.getProperty("file.separator")</code>
	 * 
	 * @return the file separator on the current platform
	 */
	public static String getFileSpearator() {
		return System.getProperty("file.separator");
	}

	/**
	 * Returns the path separator on the current platform.
	 * <p>
	 * This method refers to <code>System.getProperty("path.separator")</code>
	 * 
	 * @return the path separator on the current platform
	 */
	public static String getPathSpearator() {
		return System.getProperty("path.separator");
	}

	/**
	 * Returns the line separator on the current platform.
	 * <p>
	 * This method refers to <code>System.lineSeparator()</code>
	 * 
	 * @return the line separator on the current platform
	 */
	public static String getLineSpearator() {
		return System.lineSeparator();
	}

	/**
	 * Returns the default encoding on the current platform.
	 * <p>
	 * This method refers to <code>System.getProperty("sun.jnu.encoding")</code>. If this property does not exist, the
	 * method will return <code>Charset.defaultCharset()</code>.
	 * 
	 * @return the default encoding on the current platform
	 */
	public static String getEncoding() {
		return System.getProperty("sun.jnu.encoding", Charset.defaultCharset().name());
	}

	/**
	 * The current platform, {@link Platform#UNKNOWN} if the current platform cannot be identified.
	 */
	public static final Platform CURRENT = getCurrent();

	private static Platform getCurrent() {
		String osName = System.getProperty("os.name");

		if (osName.equals("Linux")) {
			return LINUX;
		} else if (osName.startsWith("Windows")) {
			return WINDOWS;
		} else if (osName.equals("Mac OS X")) {
			return OSX;
		} else {
			return Platform.UNKNOWN;
		}
	}

}
