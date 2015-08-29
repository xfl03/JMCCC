package com.darkyoooooo.jmccc.launch;

public class LaunchResult {
    private boolean succeed;
    private ErrorType errorType;
    private String message;
    private Throwable exceptionInstance;

    public LaunchResult(boolean succeed, ErrorType errorType, String message, Throwable throwable) {
        this.succeed = succeed;
        this.errorType = errorType;
        this.message = message;
        this.exceptionInstance = throwable;
    }

    public LaunchResult(boolean succeed, ErrorType errorType, Throwable throwable) {
        this(succeed, errorType, "", throwable);
    }

    public boolean succeed() {
        return this.succeed;
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
