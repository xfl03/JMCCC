package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import org.to2mbn.jmccc.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequester {

    private static final int TIMEOUT = 15000;

    private Proxy proxy;

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public String request(String method, String url) throws IOException {
        return request(method, url, null);
    }

    public String request(String method, String url, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = createHttpConnection(url, headers);
        connection.setRequestMethod(method);
        try {
            connection.connect();
            try (InputStream in = connection.getInputStream()) {
                return IOUtils.toString(in);
            }
        } catch (IOException e) {
            try (InputStream in = connection.getErrorStream()) {
                return readErrorStream(in, e);
            }
        } finally {
            connection.disconnect();
        }
    }

    public String requestWithPayload(String method, String url, Object payload, String contentType) throws IOException {
        return requestWithPayload(method, url, payload, contentType, null);
    }

    public String requestWithPayload(String method, String url, Object payload, String contentType, Map<String, String> headers) throws IOException {
        byte[] bytePayload;
        if (payload instanceof byte[]) {
            bytePayload = (byte[]) payload;
        } else if (payload == null) {
            bytePayload = new byte[0];
        } else {
            bytePayload = String.valueOf(payload).getBytes("UTF-8");
        }

        HttpURLConnection connection = createHttpConnection(url, headers);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Content-Length", String.valueOf(bytePayload.length));
        connection.setDoOutput(true);

        try {
            connection.connect();
            try (OutputStream out = connection.getOutputStream()) {
                out.write(bytePayload);
            }
            try (InputStream in = connection.getInputStream()) {
                return IOUtils.toString(in);
            }
        } catch (IOException e) {
            try (InputStream in = connection.getErrorStream()) {
                return readErrorStream(in, e);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String readErrorStream(InputStream in, IOException e) throws IOException {
        if (in == null) {
            throw e;
        }
        try {
            return IOUtils.toString(in);
        } catch (IOException e1) {
            if (e != e1) {
                e1.addSuppressed(e);
            }
            throw e1;
        }
    }

    private HttpURLConnection createHttpConnection(String url, Map<String, String> headers) throws UnsupportedEncodingException, MalformedURLException, IOException {
        HttpURLConnection connection = createHttpConnection(new URL(url));
        if (headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        return connection;
    }

    private HttpURLConnection createHttpConnection(URL url) throws IOException {
        Proxy usingProxy = proxy;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(usingProxy == null ? Proxy.NO_PROXY : usingProxy);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setUseCaches(false);
        return connection;
    }

}
