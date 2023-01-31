package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

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

public class RemoteVersion implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(RemoteVersion.class.getCanonicalName());

    private static final Pattern DATETIME_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})([+\\-]\\d{2}:?\\d{2})?$");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final long serialVersionUID = 1L;

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private String version;
    private Date uploadTime;
    private String uploadTimeString;
    private Date releaseTime;
    private String releaseTimeString;
    private String type;
    private String url;
    /**
     * Constructor of RemoteVersion.
     *
     * @param version           the version number
     * @param uploadTime        the upload time
     * @param uploadTimeString  the raw string of upload time
     * @param releaseTime       the release time
     * @param releaseTimeString the raw string of release time
     * @param type              the version type
     * @param url               the url of the version json
     * @throws NullPointerException if <code>version==null</code>
     */
    public RemoteVersion(String version, Date uploadTime, String uploadTimeString, Date releaseTime, String releaseTimeString, String type, String url) {
        this.version = Objects.requireNonNull(version);
        this.uploadTime = uploadTime;
        this.uploadTimeString = uploadTimeString;
        this.releaseTime = releaseTime;
        this.releaseTimeString = releaseTimeString;
        this.type = type;
        this.url = url;
    }

    public static RemoteVersion fromJson(JSONObject json) throws JSONException {
        String version = json.getString("id");
        String updateTime = json.optString("time", null);
        String releaseTime = json.optString("releaseTime", null);
        String type = json.optString("type", null);
        String url = json.optString("url", null);
        return new RemoteVersion(version,
                updateTime == null ? null : convertDate(updateTime),
                updateTime,
                releaseTime == null ? null : convertDate(releaseTime),
                releaseTime,
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
            Date localdatetime;
            synchronized (DATE_FORMAT) {
                localdatetime = DATE_FORMAT.parse(datetime);
            }

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(localdatetime);
            cal.add(Calendar.SECOND, -offsetsecs);
            return cal.getTime();
        } catch (ParseException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Couldn't parse date, skipping: " + date, e);
            return null;
        }
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

    public String getUploadTimeString() {
        return uploadTimeString;
    }

    public String getReleaseTimeString() {
        return releaseTimeString;
    }

    @Override
    public String toString() {
        return String.format("RemoteVersion [version=%s, uploadTime=%s, uploadTimeString=%s, releaseTime=%s, releaseTimeString=%s, type=%s, url=%s]", version, uploadTime, uploadTimeString, releaseTime, releaseTimeString, type, url);
    }

    @Override
    public int hashCode() {
        return version.hashCode();
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
                    Objects.equals(releaseTimeString, another.releaseTimeString) &&
                    Objects.equals(uploadTime, another.uploadTime) &&
                    Objects.equals(uploadTimeString, another.uploadTimeString) &&
                    Objects.equals(type, another.type) &&
                    Objects.equals(url, another.url);
        }
        return false;
    }

}
