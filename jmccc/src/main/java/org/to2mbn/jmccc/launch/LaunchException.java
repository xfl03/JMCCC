package org.to2mbn.jmccc.launch;

/**
 * Thrown when failing to launch the game.
 *
 * @author yushijinhun
 */
public class LaunchException extends Exception {

    private static final long serialVersionUID = 1L;

    public LaunchException() {
    }

    public LaunchException(String message, Throwable cause) {
        super(message, cause);
    }

    public LaunchException(String message) {
        super(message);
    }

    public LaunchException(Throwable cause) {
        super(cause);
    }

}
