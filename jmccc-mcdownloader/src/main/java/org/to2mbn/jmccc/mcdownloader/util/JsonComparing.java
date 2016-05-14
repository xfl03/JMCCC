package org.to2mbn.jmccc.mcdownloader.util;

import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonComparing  {
	
	public static boolean equals(Object a, Object b) {
		if (a instanceof JSONObject && b instanceof JSONObject) {
			JSONObject objA = (JSONObject) a;
			JSONObject objB = (JSONObject) b;
			if (objA.length() != objB.length()) {
				return false;
			}
			for (String key : objA.keySet()) {
				if (!objB.has(key)) {
					return false;
				}
				if (!equals(objA.get(key), objB.get(key))) {
					return false;
				}
			}
			return true;

		} else if (a instanceof JSONArray && b instanceof JSONArray) {
			JSONArray arrarA = (JSONArray) a;
			JSONArray arrarB = (JSONArray) b;
			if (arrarA.length() != arrarB.length()) {
				return false;
			}
			for (int i = 0; i < arrarA.length(); i++) {
				if (!equals(arrarA.get(i), arrarB.get(i))) {
					return false;
				}
			}
			return true;

		} else {
			return Objects.equals(a, b);
		}
	}

	private JsonComparing() {
	}
}
