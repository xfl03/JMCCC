package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

public class ServerInfo {
	@Getter private String address;
	@Getter private int port;
	
	public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
	}
}
