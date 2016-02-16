package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;

public class ResponseSignatureException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public ResponseSignatureException() {
		super();
	}

	public ResponseSignatureException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResponseSignatureException(String message) {
		super(message);
	}

	public ResponseSignatureException(Throwable cause) {
		super(cause);
	}

}
