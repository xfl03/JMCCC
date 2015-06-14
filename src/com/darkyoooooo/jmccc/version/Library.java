package com.darkyoooooo.jmccc.version;

import lombok.Getter;

public class Library {
	@Getter private String name, domain, version;
	@Getter private boolean isClientReq, isServerReq;
	
	public Library(String domain, String name, String version, boolean isClientReq, boolean isServerReq) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.isClientReq = isClientReq;
		this.isServerReq = isServerReq;
	}
}
