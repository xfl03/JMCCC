package org.to2mbn.jmccc.option;

import java.io.Serializable;
import java.util.Objects;

public class ServerInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The host of server, cannot be null
	 */
	private String host;

	/**
	 * The port of server, default to 25565
	 */
	private int port;

	/**
	 * Creates a ServerInfo with the given host and default port 25565.
	 * 
	 * @param host the host of the server
	 * @throws NullPointerException if <code>host==null</code>
	 */
	public ServerInfo(String host) {
		this(host, 25565);
	}

	/**
	 * Creates a ServerInfo with the given host and port.
	 * 
	 * @param host the host
	 * @param port the port
	 * @throws NullPointerException if <code>host==null</code>
	 * @throws IllegalArgumentException if <code>port&lt;0</code>
	 */
	public ServerInfo(String host, int port) {
		Objects.requireNonNull(host);
		if (port < 0) {
			throw new IllegalArgumentException("port<0");
		}

		this.host = host;
		this.port = port;
	}

	/**
	 * Gets the host of server.
	 * 
	 * @return the host of server
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host of server.
	 * 
	 * @param host the host to set
	 * @throws NullPointerException if <code>host==null</code>
	 */
	public void setHost(String host) {
		Objects.requireNonNull(host);
		this.host = host;
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
	 * Sets the port.
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
	public String toString() {
		return host + ':' + port;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ServerInfo) {
			ServerInfo another = (ServerInfo) obj;
			return port == another.port && host.equals(another.host);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}

}
