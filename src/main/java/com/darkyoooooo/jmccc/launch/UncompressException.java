package com.darkyoooooo.jmccc.launch;

/**
 * Signals that an exception has occurred during uncompressing.
 * 
 * @author yushijinhun
 */
public class UncompressException extends LaunchException {

    private static final long serialVersionUID = 1L;

    public UncompressException() {
    }

    public UncompressException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncompressException(String message) {
        super(message);
    }

    public UncompressException(Throwable cause) {
        super(cause);
    }

}
