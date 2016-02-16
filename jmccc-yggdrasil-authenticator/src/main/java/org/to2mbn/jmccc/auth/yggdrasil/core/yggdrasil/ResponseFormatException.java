package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;

public class ResponseFormatException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public ResponseFormatException() {
		super();
	}

	public ResponseFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResponseFormatException(String message) {
		super(message);
	}

	public ResponseFormatException(Throwable cause) {
		super(cause);
	}

}
