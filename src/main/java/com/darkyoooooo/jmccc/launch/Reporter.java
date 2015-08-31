package com.darkyoooooo.jmccc.launch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.darkyoooooo.jmccc.util.References;
import com.google.gson.Gson;

/**
 * Used to report launch argument for statistics and debugging.
 * <p>
 * The extended identity is used to identity the caller of JMCCC, default to null. If you wanna help us do the
 * statistics better, please set this to the name, and the version of your launcher.
 */
public class Reporter {

    /**
     * JMCCC Report API
     */
    private static final String reportLink = "http://darkyoooooo.sinaapp.com/update-v2.php";

    private Gson gson = new Gson();

    /**
     * used to identify client
     */
    private String useragent;

    /**
     * Creates a Report with current version and no extended identity
     */
    public Reporter() {
        this(References.ID, References.VERSION);
    }

    /**
     * Creates a Report with current version and given extended identity
     */
    public Reporter(String user) {
        this(References.ID, References.VERSION);
    }

    /**
     * Creates a Report with given version and no extended identity
     */
    public Reporter(String id, String version) {
        this(id, version, null);
    }

    /**
     * Creates a Report with given version and given extended identity
     */
    public Reporter(String id, String version, String user) {
        useragent = id + "/" + version + (user == null ? "" : "@" + user);
    }

    private void report(Object data) throws ReportException {
        try {
            // decode data
            byte[] reportData = gson.toJson(data).getBytes("UTF-8");

            // setup connection
            URL url = new URL(reportLink);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setConnectTimeout(10 * 1000);
                connection.setReadTimeout(10 * 1000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("User-Agent", useragent);
                connection.setRequestProperty("Content-length", String.valueOf(reportData.length));
                connection.setRequestProperty("Content-Type", "application/json");

                connection.connect();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (OutputStream out = connection.getOutputStream()) {
                        out.write(reportData);
                    }
                } else {
                    throw new ReportException("illegal http response code: " + responseCode);
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        } catch (IOException e) {
            throw new ReportException(e);
        }
    }

    private Map<String, String> generateBaseReport(LaunchArgument argument) {
        Map<String, String> report = new HashMap<>();
        // report.put("os", System.getProperty("os.name"));
        // map.put("java-path", Utils.getJavaPath());
        // map.put("game-root", jmccc.getBaseOptions().getGameRoot());
        // TODO
        return report;
    }

    private Map<String, String> generateSuccessfulReport(LaunchResult result) {
        Map<String, String> report = generateBaseReport(null/* TODO */);
        return report;
    }

    private Map<String, String> generateUnsuccessfulReport(LaunchArgument args, Throwable e) {
        Map<String, String> report = generateBaseReport(args);
        // TODO
        return report;
    }

    public void launchSuccessfully(LaunchResult result) throws ReportException {
        report(generateSuccessfulReport(result));
    }

    public void launchUnsuccessfully(LaunchArgument args, Throwable e) throws ReportException {
        report(generateUnsuccessfulReport(args, e));
    }

}
