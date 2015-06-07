package com.darkyoooooo.jmccc.version;

public class Library {
	private final String name, domain, version;
	private final boolean isClientReq, isServerReq;
	
	public Library(String domain, String name, String version, boolean isClientReq, boolean isServerReq) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.isClientReq = isClientReq;
		this.isServerReq = isServerReq;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isServerReq() {
		return this.isServerReq;
	}
	
	public boolean isClientReq() {
		return this.isClientReq;
	}
}
