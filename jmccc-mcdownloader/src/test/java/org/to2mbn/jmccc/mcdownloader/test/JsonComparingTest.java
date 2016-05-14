package org.to2mbn.jmccc.mcdownloader.test;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.to2mbn.jmccc.mcdownloader.util.JsonComparing;

public class JsonComparingTest {

	@Test
	public void testNullEq() {
		assertEquals(true, JsonComparing.equals(null, null));
	}

	@Test
	public void testNullNotEq() {
		assertEquals(false, JsonComparing.equals(null, new JSONObject()));
		assertEquals(false, JsonComparing.equals(new JSONObject(), null));
	}

	@Test
	public void testJsonObjectEqEmpty() {
		assertEquals(true, JsonComparing.equals(new JSONObject(), new JSONObject()));
	}

	@Test
	public void testJsonObjectEqNormal() {
		Map<String, Object> data = new HashMap<>();
		data.put("str", "strval");
		data.put("int", 2333);
		data.put("double", 2333.0d);
		data.put("null", JSONObject.NULL);
		assertEquals(true, JsonComparing.equals(new JSONObject(data), new JSONObject(data)));
	}

	@Test
	public void testJsonObjectNotEqSameLength() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);
		data2.put("nullxcc", JSONObject.NULL);

		assertEquals(false, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(false, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

	@Test
	public void testJsonObjectNotEqSameLength2() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);
		data2.put("null", true);

		assertEquals(false, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(false, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

	@Test
	public void testJsonObjectNotEqDifferentLength() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);

		assertEquals(false, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(false, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

	@Test
	public void testJsonObjectNotEqDifferentLength2() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);
		data1.put("null2", JSONObject.NULL);

		assertEquals(false, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(false, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

	@Test
	public void testJsonArrayEqEmpty() {
		assertEquals(true, JsonComparing.equals(new JSONArray(), new JSONArray()));
	}

	@Test
	public void testJsonArrayNotEqDifferentLength() {
		JSONArray o1 = new JSONArray();
		JSONArray o2 = new JSONArray(new Object[] { "233" });
		assertEquals(false, JsonComparing.equals(o1, o2));
		assertEquals(false, JsonComparing.equals(o2, o1));
	}

	@Test
	public void testJsonArrayNotEqSameLength() {
		JSONArray o1 = new JSONArray(new Object[] { JSONObject.NULL });
		JSONArray o2 = new JSONArray(new Object[] { "233" });
		assertEquals(false, JsonComparing.equals(o1, o2));
		assertEquals(false, JsonComparing.equals(o2, o1));
	}

	@Test
	public void testEqComplex() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);
		data1.put("array", new JSONArray(new Object[] { new JSONObject(), JSONObject.NULL }));

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);
		data2.put("null", JSONObject.NULL);
		data2.put("array", new JSONArray(new Object[] { new JSONObject(), JSONObject.NULL }));

		assertEquals(true, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(true, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

	@Test
	public void testNotEqComplex() {
		Map<String, Object> data1 = new HashMap<>();
		data1.put("str", "strval");
		data1.put("int", 2333);
		data1.put("double", 2333.0d);
		data1.put("null", JSONObject.NULL);
		data1.put("array", new JSONArray(new Object[] { new JSONObject(), JSONObject.NULL }));

		Map<String, Object> data2 = new HashMap<>();
		data2.put("str", "strval");
		data2.put("int", 2333);
		data2.put("double", 2333.0d);
		data2.put("null", JSONObject.NULL);

		Map<String, Object> data21 = new HashMap<>();
		data21.put("nul", JSONObject.NULL);
		data2.put("array", new JSONArray(new Object[] { new JSONObject(data21), JSONObject.NULL }));

		assertEquals(false, JsonComparing.equals(new JSONObject(data1), new JSONObject(data2)));
		assertEquals(false, JsonComparing.equals(new JSONObject(data2), new JSONObject(data1)));
	}

}
