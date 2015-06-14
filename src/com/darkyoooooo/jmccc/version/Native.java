package com.darkyoooooo.jmccc.version;

import lombok.Getter;

public class Native {
	@Getter private String name, domain, version, suffix;
	@Getter private boolean isAllowed;
	
	public Native(String domain, String name, String version, String suffix, boolean isAllowed) {
		this.name = name;
		this.domain = domain;
		this.version = version;
		this.suffix = suffix;
		this.isAllowed = isAllowed;
	}
}
