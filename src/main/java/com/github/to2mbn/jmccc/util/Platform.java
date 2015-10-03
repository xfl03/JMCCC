package com.github.to2mbn.jmccc.util;

public enum Platform {
    WINDOWS,
    LINUX,
    OSX,
    UNKNOWN;

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
     * This method refers to <code>System.getProperty("sun.jnu.encoding")</code>
     * 
     * @return the default encoding on the current platform
     */
    public static String getEncoding() {
        return System.getProperty("sun.jnu.encoding");
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
