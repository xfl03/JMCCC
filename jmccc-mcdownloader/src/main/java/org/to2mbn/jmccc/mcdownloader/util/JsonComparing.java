package org.to2mbn.jmccc.mcdownloader.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonComparing {

	private static class JsonHolder {

		public final Object obj;

		public JsonHolder(Object obj) {
			this.obj = obj;
		}

		@Override
		public boolean equals(Object another) {
			if (another instanceof JsonHolder) {
				another = ((JsonHolder) another).obj;
			}
			return JsonComparing.equals(obj, another);
		}

		@Override
		public int hashCode() {
			return JsonComparing.hashCode(obj);
		}
	}

	public static boolean equalsJsonSet(Set<?> a, Set<?> b) {
		Set<JsonHolder> as = new HashSet<>();
		Set<JsonHolder> bs = new HashSet<>();
		for (Object o : a) {
			as.add(new JsonHolder(o));
		}
		for (Object o : b) {
			bs.add(new JsonHolder(o));
		}
		return as.equals(bs);
	}

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

	public static int hashCode(Object json) {
		if (json instanceof JSONObject) {
			int h = 0;
			for (String key : ((JSONObject) json).keySet()) {
				h += key.hashCode();
				h += hashCode(((JSONObject) json).get(key));
			}
			return h;

		} else if (json instanceof JSONArray) {
			int h = 1;
			for (Object o : (JSONArray) json) {
				h = 31 * h + hashCode(o);
			}
			return h;

		} else {
			return Objects.hashCode(json);
		}
	}

	private JsonComparing() {}
}
