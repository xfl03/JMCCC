package org.to2mbn.jmccc.mcdownloader;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteVersion implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(RemoteVersion.class.getCanonicalName());

	private static final Pattern DATETIME_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})([+\\-]\\d{2}:?\\d{2})?$");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static RemoteVersion fromJson(JSONObject json) throws JSONException {
		String version = json.getString("id");
		String updateTime = json.optString("time", null);
		String releaseTime = json.optString("releaseTime", null);
		String type = json.optString("type", null);
		String url = json.optString("url", null);
		return new RemoteVersion(version,
				updateTime == null ? null : convertDate(updateTime),
				releaseTime == null ? null : convertDate(releaseTime),
				type,
				url);
	}

	private static Date convertDate(String date) {
		try {
			Matcher matcher = DATETIME_PATTERN.matcher(date);
			if (!matcher.find()) {
				throw new IllegalArgumentException("regex mismatch");
			}
			String datetime = matcher.group(1);
			String timezoneoffset = matcher.group(2).replace(":", "");
			boolean negativeoffset = timezoneoffset.charAt(0) == '-';
			int offsetsecs = Integer.parseInt(timezoneoffset.substring(1, 3)) * 3600
					+ Integer.parseInt(timezoneoffset.substring(3, 5));
			if (negativeoffset) {
				offsetsecs = -offsetsecs;
			}
			Date localdatetime = DATE_FORMAT.parse(datetime);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTime(localdatetime);
			cal.add(Calendar.SECOND, -offsetsecs);
			return cal.getTime();
		} catch (ParseException | IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Couldn't parse date, skipping: " + date, e);
			return null;
		}
	}

	private static final long serialVersionUID = 1L;

	private String version;
	private Date uploadTime;
	private Date releaseTime;
	private String type;
	private String url;

	/**
	 * Constructor of RemoteVersion.
	 * 
	 * @param version the version number
	 * @param uploadTime the upload time
	 * @param releaseTime the release time
	 * @param type the version type
	 * @param url the url of the version json
	 * @throws NullPointerException if <code>version==null</code>
	 */
	public RemoteVersion(String version, Date uploadTime, Date releaseTime, String type, String url) {
		this.version = Objects.requireNonNull(version);
		this.uploadTime = uploadTime;
		this.releaseTime = releaseTime;
		this.type = type;
		this.url = url;
	}

	/**
	 * Gets the version number.
	 * 
	 * @return the version number
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the upload time.
	 * 
	 * @return the upload time
	 */
	public Date getUploadTime() {
		return uploadTime;
	}

	/**
	 * Gets the release time.
	 * 
	 * @return the release time
	 */
	public Date getReleaseTime() {
		return releaseTime;
	}

	/**
	 * Gets the type of the version.
	 * <p>
	 * Return values could be "snapshot", "release", "old_beta", "old_alpha"
	 * 
	 * @return the type of the version
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the url of the version json, can be null.
	 * 
	 * @return the url of the version json, can be null
	 */
	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return String.format("RemoteVersion [version=%s, uploadTime=%s, releaseTime=%s, type=%s, url=%s]", version, uploadTime, releaseTime, type, url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, uploadTime, releaseTime, type, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RemoteVersion) {
			RemoteVersion another = (RemoteVersion) obj;
			return Objects.equals(version, another.version) &&
					Objects.equals(releaseTime, another.releaseTime) &&
					Objects.equals(uploadTime, another.uploadTime) &&
					Objects.equals(type, another.type) &&
					Objects.equals(url, another.url);
		}
		return false;
	}

}
