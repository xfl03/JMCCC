package org.to2mbn.jmccc.mcdownloader.download.io;

import java.io.IOException;

public final class DownloaderHelper {

    private DownloaderHelper() {
    }

    public static boolean shouldRetry(Throwable e) {
        if (!(e instanceof IOException)) {
            return false;
        }
        if (e instanceof IllegalHttpResponseCodeException) {
            return isResponseCodeRetryable(((IllegalHttpResponseCodeException) e).getResponseCode());
        }
        return true;
    }

    public static boolean isResponseCodeRetryable(int responseCode) {
        if (responseCode >= 400 && responseCode <= 499) { // 4xx
            if (responseCode == 408 // Request Timeout
            ) {
                return true;
            } else {
                return false;
            }
        } else if (responseCode >= 500 && responseCode <= 599) { // 5xx
            if (responseCode == 500 || // Internal Server Error
                    responseCode == 505 || // HTTP Version Not Supported
                    responseCode == 506 // Variant Also Negotiates (RFC 2295)
            ) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}
