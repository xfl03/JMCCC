package org.to2mbn.jmccc.util;

public enum Arch {
    DEFAULT, X86, ARM64;

    public static final Arch CURRENT = inferArch(System.getProperty("os.arch"));
    public static final String SIMPLE = Platform.isX64() ? "64" : "32";

    public static Arch inferArch(String archName) {
        if (archName == null || archName.equals("")) {
            return DEFAULT;
        }
        //Refer: https://github.com/openjdk/jdk/blob/master/src/hotspot/os/bsd/os_bsd.cpp#L182-L192
        switch (archName) {
            case "aarch64":
            case "arm64":
                return ARM64;
            case "i386":
            case "i486":
            case "x86":
            case "32":
                return X86;
            default:
                return DEFAULT;
        }
    }
}
