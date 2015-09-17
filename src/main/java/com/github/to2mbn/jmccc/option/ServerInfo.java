package com.github.to2mbn.jmccc.option;

import java.util.Objects;

public class ServerInfo {

    /**
     * The address of the server, cannot be null
     */
    private String address;

    /**
     * The port of the server, default to 25565
     */
    private int port;

    /**
     * Creates a ServerInfo with the given address and default port 25565
     * 
     * @param address the address of the server
     * @throws NullPointerException if <code>address==null</code>
     */
    public ServerInfo(String address) {
        this(address, 25565);
    }

    /**
     * Creates a ServerInfo with the given address and the given port
     * 
     * @param address the address of the server
     * @param port the port of the server
     * @throws NullPointerException if <code>address==null</code>
     * @throws IllegalArgumentException if <code>port&lt;0</code>
     */
    public ServerInfo(String address, int port) {
        Objects.requireNonNull(address);
        if (port < 0) {
            throw new IllegalArgumentException("port<0");
        }

        this.address = address;
        this.port = port;
    }

    /**
     * Gets the server address
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the server address
     * 
     * @param address the address to set
     * @throws NullPointerException if <code>address==null</code>
     */
    public void setAddress(String address) {
        Objects.requireNonNull(address);
        this.address = address;
    }

    @Override
    public String toString() {
        return address + ':' + port;
    }

    /**
     * Gets the server port
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the server port
     * 
     * @param port the port to set
     * @throws IllegalArgumentException if <code>port&lt;0</code>
     */
    public void setPort(int port) {
        if (port < 0) {
            throw new IllegalArgumentException("port<0");
        }

        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ServerInfo) {
            ServerInfo another = (ServerInfo) obj;
            return port == another.port && address.equals(another.address);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

}
