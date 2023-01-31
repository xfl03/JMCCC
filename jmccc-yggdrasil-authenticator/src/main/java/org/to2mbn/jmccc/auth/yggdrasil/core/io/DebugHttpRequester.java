package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DebugHttpRequester extends HttpRequester {

    private static final Logger LOGGER = Logger.getLogger(DebugHttpRequester.class.getCanonicalName());

    private static void debug_request(String method, String url, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder("request:\n");
        sb.append(method).append(' ').append(url).append('\n');
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }
        }
        LOGGER.info(sb.toString());
    }

    private static void debug_requestWithPayload(String method, String url, Object payload, String contentType, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder("request:\n");
        sb.append(method).append(' ').append(url).append('\n');
        sb.append("Content-Type: ").append(contentType).append('\n');
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }
        }
        sb.append('\n');
        if (payload instanceof byte[]) {
            sb.append('[').append(((byte[]) payload).length).append(" bytes]");
        } else if (payload instanceof JSONObject) {
            sb.append(((JSONObject) payload).toString(4));
        } else if (payload instanceof JSONArray) {
            sb.append(((JSONArray) payload).toString(4));
        } else {
            sb.append(payload);
        }
        LOGGER.info(sb.toString());
    }

    private static String debug_response(String response) {
        StringBuilder sb = new StringBuilder("response:\n");
        String toDisplay = null;
        try {
            toDisplay = new JSONObject(response).toString(4);
        } catch (JSONException e) {
            try {
                toDisplay = new JSONArray(response).toString(4);
            } catch (JSONException e1) {
            }
        }
        sb.append(toDisplay == null ? response : toDisplay);
        LOGGER.info(sb.toString());
        return response;
    }

    private static void debug_error(Throwable e) {
        LOGGER.log(Level.INFO, "error:", e);
    }

    @Override
    public String request(String method, String url, Map<String, String> headers) throws IOException {
        debug_request(method, url, headers);
        try {
            return debug_response(super.request(method, url, headers));
        } catch (IOException e) {
            debug_error(e);
            throw e;
        }
    }

    @Override
    public String requestWithPayload(String method, String url, Object payload, String contentType, Map<String, String> headers) throws IOException {
        debug_requestWithPayload(method, url, payload, contentType, headers);
        try {
            return debug_response(super.requestWithPayload(method, url, payload, contentType, headers));
        } catch (IOException e) {
            debug_error(e);
            throw e;
        }
    }

}
