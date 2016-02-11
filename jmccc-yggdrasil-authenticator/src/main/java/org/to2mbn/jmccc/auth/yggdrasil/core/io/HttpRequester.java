package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequester {

	private static final int TIMEOUT = 15000;
	private static final int BUFFER_SIZE = 8192;

	private Proxy proxy;

	public HttpRequester() {
		this(null);
	}

	public HttpRequester(Proxy proxy) {
		this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public String get(String url, Map<String, Object> arguments) throws UnsupportedEncodingException, MalformedURLException, IOException {
		HttpURLConnection connection = createHttpConnection(url, arguments);
		connection.setRequestMethod("GET");
		try {
			connection.connect();
			try (InputStream in = connection.getInputStream()) {
				return read(in);
			}
		} catch (IOException e) {
			try (InputStream in = connection.getErrorStream()) {
				return readErrorStream(in, e);
			}
		} finally {
			connection.disconnect();
		}
	}

	public String post(String url, Map<String, Object> arguments, String post, String contentType) throws UnsupportedEncodingException, MalformedURLException, IOException {
		byte[] rawpost = post.getBytes("UTF-8");

		HttpURLConnection connection = createHttpConnection(url, arguments);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
		connection.setRequestProperty("Content-Length", String.valueOf(rawpost.length));
		connection.setDoOutput(true);

		try {
			connection.connect();
			try (OutputStream out = connection.getOutputStream()) {
				out.write(rawpost);
			}
			try (InputStream in = connection.getInputStream()) {
				return read(in);
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
			return read(in);
		} catch (IOException e1) {
			if (e != e1) {
				e1.addSuppressed(e);
			}
			throw e1;
		}
	}

	private String read(InputStream in) throws UnsupportedEncodingException, IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[BUFFER_SIZE];
		Reader reader = new InputStreamReader(in, "UTF-8");
		int read;
		while ((read = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, read);
		}
		return sb.toString();
	}

	private HttpURLConnection createHttpConnection(String baseurl, Map<String, Object> arguments) throws UnsupportedEncodingException, MalformedURLException, IOException {
		String url = baseurl;
		if (arguments != null) {
			url = url + "?" + generateArguments(arguments);
		}
		return createHttpConnection(new URL(url));
	}

	private HttpURLConnection createHttpConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);
		connection.setUseCaches(false);
		return connection;
	}

	private String generateArguments(Map<String, Object> arguments) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		for (Entry<String, Object> argument : arguments.entrySet()) {
			result.append(URLEncoder.encode(argument.getKey(), "UTF-8"));
			result.append('=');
			result.append(URLEncoder.encode(String.valueOf(argument.getValue()), "UTF-8"));
			result.append('&');
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}

}
