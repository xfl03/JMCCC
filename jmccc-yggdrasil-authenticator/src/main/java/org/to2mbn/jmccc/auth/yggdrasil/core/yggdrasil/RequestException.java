package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;

public class RequestException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public RequestException() {
		super();
	}

	public RequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestException(String message) {
		super(message);
	}

	public RequestException(Throwable cause) {
		super(cause);
	}

}
