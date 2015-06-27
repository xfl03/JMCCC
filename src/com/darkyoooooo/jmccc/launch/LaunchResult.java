package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

public class LaunchResult {
	@Getter private boolean isSucceed;
	@Getter private ErrorType errorType;
	@Getter private String message;
	@Getter private Throwable exceptionInstance;
	
	public LaunchResult(boolean isSucceed, ErrorType errorType, String message, Throwable t) {
		this.isSucceed = isSucceed;
		this.errorType = errorType;
		this.message = message;
		this.exceptionInstance = t;
	}
	
	public LaunchResult(boolean isSucceed, ErrorType errorType, Throwable t) {
		this(isSucceed, errorType, "", t);
	}
}
