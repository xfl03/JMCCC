package com.darkyoooooo.jmccc.util;

public class BaseOptions {
    private final String gameRoot, javaPath;

    public BaseOptions(String gameRoot, String javaPath) {
        this.gameRoot = gameRoot;
        this.javaPath = javaPath;
    }

    public BaseOptions(String gameRoot) {
        this(gameRoot, Utils.getJavaPath());
    }

    public BaseOptions() {
        this(Utils.handlePath(".minecraft"), Utils.getJavaPath());
    }

    public String getGameRoot() {
        return this.gameRoot;
    }

    public String getJavaPath() {
        return this.javaPath;
    }
}
