package com.darkyoooooo.jmccc.version;

public class Native {
	private String name, domain, version, suffix;
	private boolean isAllowed;
	
	public Native(String domain, String name, String version, String suffix, boolean isAllowed) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.suffix = suffix;
		this.isAllowed = isAllowed;
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

	public String getSuffix() {
		return this.suffix;
	}

	public boolean isAllowed() {
		return this.isAllowed;
	}
}
