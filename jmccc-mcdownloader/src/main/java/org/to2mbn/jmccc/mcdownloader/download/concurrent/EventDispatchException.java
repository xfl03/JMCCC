package org.to2mbn.jmccc.mcdownloader.download.concurrent;

public class EventDispatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventDispatchException() {
        super();
    }

    public EventDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventDispatchException(String message) {
        super(message);
    }

    public EventDispatchException(Throwable cause) {
        super(cause);
    }
}
