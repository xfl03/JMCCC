package com.github.to2mbn.jmccc.launch;

/**
 * Signals that an exception has occurred during authentication.
 * 
 * @author yushijinhun
 */
public class AuthenticationException extends LaunchException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

}
