package com.darkyoooooo.jmccc.version;

public class Library {
	private String name, domain, version;
	private boolean isClientReq, isServerReq;

	public Library(String domain, String name, String version, boolean isClientReq, boolean isServerReq) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.isClientReq = isClientReq;
		this.isServerReq = isServerReq;
	}

	public boolean isServerReq() {
		return this.isServerReq;
	}

	public String getName() {
		return this.name;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getVersion() {
		return this.version;
	}

	public boolean isClientReq() {
		return this.isClientReq;
	}
}
