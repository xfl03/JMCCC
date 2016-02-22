package org.to2mbn.jmccc.mcdownloader;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteVersionList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Pattern DATETIME_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})([+\\-]\\d{2}:?\\d{2})?$");

	public static RemoteVersionList fromJson(JSONObject json) throws JSONException, ParseException {
		String latestSnapshot = null;
		String latestRelease = null;
		if (json.has("latest")) {
			JSONObject latest = json.getJSONObject("latest");
			latestSnapshot = latest.optString("snapshot");
			latestRelease = latest.optString("release");
		}

		JSONArray jsonVersions = json.getJSONArray("versions");
		Map<String, RemoteVersion> versions = new HashMap<>();
		for (int i = 0; i < jsonVersions.length(); i++) {
			JSONObject jsonVersion = jsonVersions.getJSONObject(i);
			String version = jsonVersion.getString("id");
			String updateTime = jsonVersion.optString("time", null);
			String releaseTime = jsonVersion.optString("releaseTime", null);
			String type = jsonVersion.optString("type", null);
			String url = jsonVersion.optString("url", null);
			versions.put(version, new RemoteVersion(version,
					updateTime == null ? null : convertDate(updateTime),
					updateTime == null ? null : convertDate(releaseTime),
					type,
					url));
		}
		return new RemoteVersionList(latestSnapshot, latestRelease, versions);
	}

	private static Date convertDate(String date) throws ParseException {
		Matcher matcher = DATETIME_PATTERN.matcher(date);
		if (!matcher.find()) {
			throw new ParseException("regex mismatch", 0);
		}
		String datetime = matcher.group(1);
		String timezoneoffset = matcher.group(2).replace(":", "");
		boolean negativeoffset = timezoneoffset.charAt(0) == '-';
		int offsetsecs = Integer.parseInt(timezoneoffset.substring(1, 3)) * 3600 + Integer.parseInt(timezoneoffset.substring(3, 5));
		if (negativeoffset) {
			offsetsecs = -offsetsecs;
		}
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		datetimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date localdatetime = datetimeFormat.parse(datetime);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(localdatetime);
		cal.add(Calendar.SECOND, -offsetsecs);
		Date result = cal.getTime();
		return result;
	}

	private String latestSnapshot;
	private String latestRelease;
	private Map<String, RemoteVersion> versions;

	/**
	 * Constructor of RemoteVersionList.
	 * 
	 * @param latestSnapshot the version of the latest snapshot
	 * @param latestRelease the version of the latest release
	 * @param versions the versions
	 * @throws NullPointerException <code>versions==null</code>
	 */
	public RemoteVersionList(String latestSnapshot, String latestRelease, Map<String, RemoteVersion> versions) {
		Objects.requireNonNull(versions);
		this.latestSnapshot = latestSnapshot;
		this.latestRelease = latestRelease;
		this.versions = versions;
	}

	/**
	 * Gets the version of the latest snapshot.
	 * 
	 * @return the version of the latest snapshot
	 */
	public String getLatestSnapshot() {
		return latestSnapshot;
	}

	/**
	 * Gets the version of the latest release.
	 * 
	 * @return the version of the latest release
	 */
	public String getLatestRelease() {
		return latestRelease;
	}

	/**
	 * Gets all the versions.
	 * 
	 * @return all the versions
	 */
	public Map<String, RemoteVersion> getVersions() {
		return versions;
	}

	@Override
	public String toString() {
		return "[latestSnapshot=" + latestSnapshot + ", latestRelease=" + latestRelease + ", versions=" + versions + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(latestSnapshot, latestRelease, versions);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RemoteVersionList) {
			RemoteVersionList another = (RemoteVersionList) obj;
			return versions.equals(another.versions) && Objects.equals(latestRelease, another.latestRelease) && Objects.equals(latestSnapshot, another.latestSnapshot);
		}
		return false;
	}
}
