package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONHttpRequester extends HttpRequester {

	private static final String CONTENT_TYPE_JSON = "application/json";

	public JSONHttpRequester() {
	}

	public JSONHttpRequester(Proxy proxy) {
		super(proxy);
	}

	public Object jsonPost(String url, Map<String, Object> arguments, JSONObject post) throws JSONException, UnsupportedEncodingException, MalformedURLException, IOException {
		return toJson(post(url, arguments, post.toString(), CONTENT_TYPE_JSON));
	}

	public Object jsonPost(String url, Map<String, Object> arguments, JSONArray post) throws JSONException, UnsupportedEncodingException, MalformedURLException, IOException {
		return toJson(post(url, arguments, post.toString(), CONTENT_TYPE_JSON));
	}

	public Object jsonGet(String url, Map<String, Object> arguments) throws JSONException, UnsupportedEncodingException, MalformedURLException, IOException {
		return toJson(get(url, arguments));
	}

	private Object toJson(String json) {
		if (json == null || json.trim().isEmpty()) {
			return null;
		}
		JSONTokener tokener = new JSONTokener(json);
		char next = tokener.nextClean();
		tokener.back();
		if (next == '{') {
			return new JSONObject(tokener);
		} else if (next == '[') {
			return new JSONArray(tokener);
		} else {
			throw new JSONException("Not in json format: " + json);
		}
	}

}
