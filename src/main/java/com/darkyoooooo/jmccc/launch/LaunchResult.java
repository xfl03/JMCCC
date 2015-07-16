package com.darkyoooooo.jmccc.launch;

public class LaunchResult {
	private boolean isSucceed;
	private ErrorType errorType;
	private String message;
	private Throwable exceptionInstance;
	
	public LaunchResult(boolean isSucceed, ErrorType errorType, String message, Throwable throwable) {
		this.isSucceed = isSucceed;
		this.errorType = errorType;
		this.message = message;
		this.exceptionInstance = throwable;
	}
	
	public LaunchResult(boolean isSucceed, ErrorType errorType, Throwable throwable) {
		this(isSucceed, errorType, "", throwable);
	}

	public boolean isSucceed() {
		return this.isSucceed;
	}

	public ErrorType getErrorType() {
		return this.errorType;
	}

	public String getMessage() {
		return this.message;
	}

	public Throwable getExceptionInstance() {
		return this.exceptionInstance;
	}
}
