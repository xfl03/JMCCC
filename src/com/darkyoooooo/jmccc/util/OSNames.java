package com.darkyoooooo.jmccc.util;

import lombok.Getter;

public enum OSNames {
	WINDOWS('\\', ';'),
	LINUX('/', ':'),
	OSX('/', ':'),
	UNKNOWN('/', ':');
	
	@Getter private final char fileSpearator, pathSpearator;
	public static final OSNames CURRENT = getCurrent();
	
	private OSNames(char fileSpearator, char pathSpearator) {
		this.fileSpearator = fileSpearator;
		this.pathSpearator = pathSpearator;
	}
	
	private static OSNames getCurrent() {
		String name = System.getProperty("os.name").toLowerCase();
		if(name.contains("win")) return WINDOWS;
		else if(name.contains("linux")) return LINUX;
		else if(name.contains("osx")) return OSX;
		else return UNKNOWN;
	}
}
