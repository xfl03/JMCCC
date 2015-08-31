package com.darkyoooooo.jmccc.launch;

/**
 * Signals that an exception has occurred during login.
 * 
 * @author yushijinhun
 */
public class LoginException extends LaunchException {

    private static final long serialVersionUID = 1L;

    public LoginException() {
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }

}
