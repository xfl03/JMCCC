package org.to2mbn.jmccc.mcdownloader.download.io;

import java.io.IOException;

public class IllegalHttpResponseCodeException extends IOException {

    private static final long serialVersionUID = 1L;

    private final int responseCode;

    public IllegalHttpResponseCodeException(int responseCode) {
        super(String.valueOf(responseCode));
        this.responseCode = responseCode;
    }

    public IllegalHttpResponseCodeException(String message, Throwable cause, int responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public IllegalHttpResponseCodeException(String message, int responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public IllegalHttpResponseCodeException(Throwable cause, int responseCode) {
        super(String.valueOf(responseCode), cause);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

}
