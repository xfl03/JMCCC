package com.darkyoooooo.jmccc.util;

public enum OsTypes {
	WINDOWS('\\', ';'),
	LINUX('/', ':'),
	OSX('/', ':'),
	UNKNOWN('\\', ';');
	
	private final char fileSpearator, pathSpearator;
	public static final OsTypes CURRENT = getCurrent();
	
	private OsTypes(char fileSpearator, char pathSpearator) {
		this.fileSpearator = fileSpearator;
		this.pathSpearator = pathSpearator;
	}
	
	private static OsTypes getCurrent() {
		String name = System.getProperty("os.name").toLowerCase();
		if(name.contains("win")) return WINDOWS;
		else if(name.contains("linux")) return LINUX;
		else if(name.contains("osx")) return OSX;
		else return UNKNOWN;
	}

	public char getFileSpearator() {
		return this.fileSpearator;
	}

	public char getPathSpearator() {
		return this.pathSpearator;
	}
}
