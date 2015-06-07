package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

public class ServerInfo {
	@Getter private final String address;
	@Getter private final int port;
	
	public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
	}
	
	@Override
	public String toString() {
		return this.address + ":" + this.port;
	}
}
