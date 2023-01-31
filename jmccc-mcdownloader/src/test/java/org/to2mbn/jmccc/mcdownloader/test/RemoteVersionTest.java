package org.to2mbn.jmccc.mcdownloader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.RemoteVersion;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RemoteVersionTest {

    private String id;
    private String type;
    private Date uploadTime;
    private String uploadTimeString;
    private Date releaseTime;
    private String releaseTimeString;
    private String url;
    private String json;
    public RemoteVersionTest(String id, String type, Date uploadTime, String uploadTimeString, Date releaseTime, String releaseTimeString, String url, String json) {
        this.id = id;
        this.type = type;
        this.uploadTime = uploadTime;
        this.uploadTimeString = uploadTimeString;
        this.releaseTime = releaseTime;
        this.releaseTimeString = releaseTimeString;
        this.url = url;
        this.json = json;
    }

    private static Date date(String datetime, String timezone) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(timezone));
        return format.parse(datetime);
    }

    @Parameters
    public static List<Object[]> data() throws ParseException {
        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[]{"1.9.4",
                "release",
                date("2016-05-10 14:45:26", "UTC"),
                "2016-05-10T14:45:26+00:00",
                date("2016-05-10 10:17:16", "UTC"),
                "2016-05-10T10:17:16+00:00",
                "https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
                "{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"releaseTime\":\"2016-05-10T10:17:16+00:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}"});
        data.add(new Object[]{"1.9.4",
                "release",
                date("2016-05-10 14:45:26", "UTC"),
                "2016-05-10T14:45:26+00:00",
                date("2016-05-10 10:17:16", "GMT-1:00"),
                "2016-05-10T10:17:16-01:00",
                "https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
                "{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"releaseTime\":\"2016-05-10T10:17:16-01:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}"});
        data.add(new Object[]{"1.9.4",
                "release",
                date("2016-05-10 14:45:26", "UTC"),
                "2016-05-10T14:45:26+00:00",
                null,
                null,
                "https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json",
                "{\"id\":\"1.9.4\",\"type\":\"release\",\"time\":\"2016-05-10T14:45:26+00:00\",\"url\":\"https://launchermeta.mojang.com/mc/game/cdcd308b7cbd15bc595850ce6557d4ade48cee7a/1.9.4.json\"}"});
        data.add(new Object[]{"1.9.4",
                null,
                null,
                null,
                null,
                null,
                null,
                "{\"id\":\"1.9.4\"}"});
        return data;
    }

    @Test
    public void testFromJson() {
        assertEquals(RemoteVersion.fromJson(new JSONObject(json)),
                new RemoteVersion(id, uploadTime, uploadTimeString, releaseTime, releaseTimeString, type, url));
    }

    @Test
    public void testSerializion() throws ClassNotFoundException, IOException {
        SerializationTestUtils.testSerialization(new RemoteVersion(id, uploadTime, uploadTimeString, releaseTime, releaseTimeString, type, url));
    }

}
