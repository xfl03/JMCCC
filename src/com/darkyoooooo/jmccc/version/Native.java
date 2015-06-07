package com.darkyoooooo.jmccc.version;

public class Native {
	private final String name, domain, version, suffix;
	private final boolean isAllowed;
	
	public Native(String domain, String name, String version, String suffix, boolean isAllowed) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.suffix = suffix;
		this.isAllowed = isAllowed;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isAllowed() {
		return this.isAllowed;
	}
}
