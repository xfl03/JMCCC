package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

public class LaunchResult {
	@Getter private final boolean isSucceed;
	@Getter private final ErrorType errorType;
	@Getter private final String message;
	
	public LaunchResult(boolean isSucceed, ErrorType errorType, String message) {
		this.isSucceed = isSucceed;
		this.errorType = errorType;
		this.message = message;
	}
	
	public LaunchResult(boolean isSucceed, ErrorType errorType) {
		this(isSucceed, errorType, "");
	}
}
