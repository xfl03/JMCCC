package com.darkyoooooo.jmccc.launch;

import com.darkyoooooo.jmccc.ext.GameProcessMonitor;

public class LaunchResult {

    public static LaunchResult launchSuccessfully(GameProcessMonitor monitor) {
        return new LaunchResult(true, null, null, null, monitor);
    }

    public static LaunchResult launchUnsuccessfully(ErrorType errorType, String message, Throwable throwable) {
        return new LaunchResult(false, errorType, message, throwable, null);
    }

    public static LaunchResult launchUnsuccessfully(ErrorType errorType, String message) {
        return launchUnsuccessfully(errorType, message, null);
    }

    private boolean succeed;
    private ErrorType errorType;
    private String message;
    private Throwable exceptionInstance;
    private GameProcessMonitor gameMonitor;

    private LaunchResult(boolean succeed, ErrorType errorType, String message, Throwable throwable, GameProcessMonitor gameMonitor) {
        this.succeed = succeed;
        this.errorType = errorType;
        this.message = message;
        this.exceptionInstance = throwable;
        this.gameMonitor = gameMonitor;
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

    public GameProcessMonitor getGameMonitor() {
        return gameMonitor;
    }

}
