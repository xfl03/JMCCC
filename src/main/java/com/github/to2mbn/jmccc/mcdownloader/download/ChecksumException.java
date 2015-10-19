package com.github.to2mbn.jmccc.mcdownloader.download;

import java.security.GeneralSecurityException;

/**
 * This exception indicates that the checksum is invalid or missing.
 * 
 * @author yushijinhun
 */
public class ChecksumException extends GeneralSecurityException {

	private static final long serialVersionUID = 1L;

	public ChecksumException() {
	}

	public ChecksumException(String msg) {
		super(msg);
	}

	public ChecksumException(Throwable cause) {
		super(cause);
	}

	public ChecksumException(String message, Throwable cause) {
		super(message, cause);
	}

}
