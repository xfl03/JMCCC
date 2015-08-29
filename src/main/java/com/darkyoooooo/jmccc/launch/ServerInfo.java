package com.darkyoooooo.jmccc.launch;

public class ServerInfo {
    private String address;
    private int port;

    public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }
}
