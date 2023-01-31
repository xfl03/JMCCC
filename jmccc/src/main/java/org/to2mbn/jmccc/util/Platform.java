package org.to2mbn.jmccc.util;

import java.nio.charset.Charset;

public enum Platform {
    WINDOWS, LINUX, OSX, UNKNOWN;

    /**
     * The current platform, {@link Platform#UNKNOWN} if the current platform
     * cannot be identified.
     */
    public static final Platform CURRENT = inferPlatform(System.getProperty("os.name"));

    /**
     * Returns the file separator of current platform.
     * <p>
     * This method refers to <code>System.getProperty("file.separator")</code>
     *
     * @return the file separator of current platform
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * Returns the path separator of current platform.
     * <p>
     * This method refers to <code>System.getProperty("path.separator")</code>
     *
     * @return the path separator of current platform
     */
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }

    /**
     * Returns the line separator of current platform.
     * <p>
     * This method refers to <code>System.lineSeparator()</code>
     *
     * @return the line separator of current platform
     */
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * Returns the default encoding of current platform.
     * <p>
     * This method refers to <code>System.getProperty("sun.jnu.encoding")</code>
     * . If this property does not exist, the method will return
     * <code>Charset.defaultCharset()</code>.
     *
     * @return the default encoding of current platform
     */
    public static String getEncoding() {
        return System.getProperty("sun.jnu.encoding", Charset.defaultCharset().name());
    }

    /**
     * Returns true if current platform supports x64.
     * <p>
     * This method checks <code>sun.arch.data.model</code> property first. If
     * the property does not exist, this method will check <code>os.arch</code>.
     *
     * @return true if current platform supports x64
     */
    public static boolean isX64() {
        String sunArchDataModel = System.getProperty("sun.arch.data.model");
        if (sunArchDataModel != null) {
            return "64".equals(sunArchDataModel);
        }
        return System.getProperty("os.arch").contains("64");
    }

    public static Platform inferPlatform(String osName) {
        if (osName == null) return UNKNOWN;
        osName = osName.toLowerCase();

        if (osName.contains("linux") || osName.contains("unix")) {
            return LINUX;
        } else if (osName.contains("osx") || osName.contains("os x") || osName.contains("mac")) {
            return OSX;
        } else if (osName.contains("windows")) {
            return WINDOWS;
        } else {
            return UNKNOWN;
        }
    }

}
