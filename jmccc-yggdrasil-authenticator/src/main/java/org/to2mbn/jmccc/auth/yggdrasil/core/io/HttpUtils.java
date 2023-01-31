package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public final class HttpUtils {

    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private HttpUtils() {
    }

    public static String encodeForm(Map<String, Object> form) {
        try {
            StringBuilder result = new StringBuilder();
            for (Entry<String, Object> argument : form.entrySet()) {
                result.append(URLEncoder.encode(argument.getKey(), "UTF-8"));
                result.append('=');
                result.append(URLEncoder.encode(String.valueOf(argument.getValue()), "UTF-8"));
                result.append('&');
            }
            result.deleteCharAt(result.length() - 1);
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String withUrlArguments(String url, Map<String, Object> arguments) {
        Objects.requireNonNull(url);
        if (arguments == null) {
            return url;
        }
        return url + "?" + encodeForm(arguments);
    }

}
