package org.to2mbn.jmccc.mcdownloader.test;

import static org.junit.Assert.*;
import java.util.Collections;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.junit.Test;
import org.to2mbn.jmccc.mcdownloader.RemoteVersion;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;

public class RemoteVersionListTest {

	@Test
	public void testFromJsonNormal() {
		assertEquals(new RemoteVersionList("latest-snapshot", "latest-release", Collections.<String, RemoteVersion> emptyMap()),
				RemoteVersionList.fromJson(new JSONObject("{\"latest\":{\"snapshot\":\"latest-snapshot\",\"release\":\"latest-release\"},\"versions\":[]}")));
	}

	@Test
	public void testFromJsonNullable() {
		assertEquals(new RemoteVersionList("latest-snapshot", null, Collections.<String, RemoteVersion> emptyMap()),
				RemoteVersionList.fromJson(new JSONObject("{\"latest\":{\"snapshot\":\"latest-snapshot\"},\"versions\":[]}")));
	}

}
