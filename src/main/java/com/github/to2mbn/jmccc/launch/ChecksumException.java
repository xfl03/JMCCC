package com.github.to2mbn.jmccc.launch;

/**
 * Signals that an exception has occurred during checksum verifying.
 * 
 * @author yushijinhun
 */
public class ChecksumException extends LaunchException {

    private static final long serialVersionUID = 1L;

    public ChecksumException() {
    }

    public ChecksumException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChecksumException(String message) {
        super(message);
    }

    public ChecksumException(Throwable cause) {
        super(cause);
    }

}
