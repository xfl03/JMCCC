package com.github.to2mbn.jmccc.util;

public enum OsTypes {
    WINDOWS,
    LINUX,
    OSX,
    UNKNOWN;

    public static final OsTypes CURRENT = getCurrent();

    private static OsTypes getCurrent() {
        String osName = System.getProperty("os.name");

        if (osName.equals("Linux")) {
            return LINUX;
        } else if (osName.startsWith("Windows")) {
            return WINDOWS;
        } else if (osName.equals("Mac OS X")) {
            return OSX;
        } else {
            return OsTypes.UNKNOWN;
        }
    }

    public String getFileSpearator() {
        return System.getProperty("file.separator");
    }

    public String getPathSpearator() {
        return System.getProperty("path.separator");
    }

    public String getLineSpearator() {
        return System.getProperty("line.separator");
    }
}
