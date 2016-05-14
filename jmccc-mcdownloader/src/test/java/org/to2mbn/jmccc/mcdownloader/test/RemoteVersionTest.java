package org.to2mbn.jmccc.mcdownloader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.to2mbn.jmccc.mcdownloader.RemoteVersion;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONObject;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RemoteVersionTest {

	private static Date date(String datetime, String timezone) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone(timezone));
		return format.parse(datetime);
	}

	@Parameters
	public static List<Object[]> data() throws ParseException {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(new Object[] { "1.9.4",
				"release",
				date("2016-05-10 14:45:26", "UTC"),
				date("2016-05-10 10:17:16", "UTC"),
				"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
				"{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"releaseTime\":\"2016-05-10T10:17:16+00:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}" });
		data.add(new Object[] { "1.9.4",
				"release",
				date("2016-05-10 14:45:26", "UTC"),
				date("2016-05-10 10:17:16", "GMT-1:00"),
				"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
				"{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"releaseTime\":\"2016-05-10T10:17:16-01:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}" });
		data.add(new Object[] { "1.9.4",
				"release",
				date("2016-05-10 14:45:26", "UTC"),
				null,
				"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
				"{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}" });
		data.add(new Object[] { "1.9.4",
				null,
				null,
				null,
				null,
				"{\"id\":\"1.9.4\"}" });
		return data;
	}

	private String id;
	private String type;
	private Date uploadTime;
	private Date releaseTime;
	private String url;
	private String json;

	public RemoteVersionTest(String id, String type, Date uploadTime, Date releaseTime, String url, String json) {
		this.id = id;
		this.type = type;
		this.uploadTime = uploadTime;
		this.releaseTime = releaseTime;
		this.url = url;
		this.json = json;
	}

	@Test
	public void testFromJson() {
		assertEquals(RemoteVersion.fromJson(new JSONObject(json)),
				new RemoteVersion(id, uploadTime, releaseTime, type, url));
	}

	@Test
	public void testSerializion() throws ClassNotFoundException, IOException {
		SerializationTestUtils.testSerialization(new RemoteVersion(id, uploadTime, releaseTime, type, url));
	}

}
