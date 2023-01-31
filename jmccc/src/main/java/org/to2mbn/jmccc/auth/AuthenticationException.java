package org.to2mbn.jmccc.auth;

import org.to2mbn.jmccc.launch.LaunchException;

/**
 * Thrown when authentication fails.
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
