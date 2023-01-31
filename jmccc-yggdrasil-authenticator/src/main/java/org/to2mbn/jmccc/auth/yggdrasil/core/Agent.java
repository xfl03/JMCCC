package org.to2mbn.jmccc.auth.yggdrasil.core;

public enum Agent {

    MINECRAFT("Minecraft", 1),
    SCROLLS("Scrolls", 1);

    private String name;
    private int version;

    private Agent(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

}
