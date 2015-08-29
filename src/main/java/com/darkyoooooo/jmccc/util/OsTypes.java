package com.darkyoooooo.jmccc.util;

public enum OsTypes {
    WINDOWS,
    LINUX,
    OSX,
    UNKNOWN;
    public static OsTypes CURRENT() {
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("win")) return WINDOWS;
        else if (name.contains("linux")) return LINUX;
        else if (name.contains("osx")) return OSX;
        else return UNKNOWN;
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
