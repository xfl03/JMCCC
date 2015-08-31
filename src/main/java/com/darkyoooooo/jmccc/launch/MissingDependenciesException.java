package com.darkyoooooo.jmccc.launch;

/**
 * Signals that dependent natives or libraries were missing.
 * 
 * @author yushijinhun
 */
public class MissingDependenciesException extends LaunchException {

    private static final long serialVersionUID = 1L;

    public MissingDependenciesException() {
    }

    public MissingDependenciesException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingDependenciesException(String message) {
        super(message);
    }

    public MissingDependenciesException(Throwable cause) {
        super(cause);
    }

}
