package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;

public class ProfileNotFoundException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public ProfileNotFoundException() {
    }

    public ProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(Throwable cause) {
        super(cause);
    }

}
