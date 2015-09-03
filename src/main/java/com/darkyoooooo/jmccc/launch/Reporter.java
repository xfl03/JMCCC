package com.darkyoooooo.jmccc.launch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.darkyoooooo.jmccc.option.LaunchOption;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.References;
import com.darkyoooooo.jmccc.util.Utils;
import com.google.gson.Gson;

/**
 * Used to report launch argument for statistics and debugging.
 * <p>
 * The extended identity is used to identity the caller of JMCCC, default to null. If you wanna help us do the
 * statistics better, please set this to the name, and the version of your launcher.
 */
class Reporter {

    /**
     * JMCCC Report API
     */
    private static final String reportLink = "http://darkyoooooo.sinaapp.com/update-v2.php";

    private Gson gson = new Gson();

    /**
     * used to identify client
     */
    private String extendedIdentity;

    /**
     * Creates a Report with current version and no extended identity
     */
    public Reporter() {
        this(null);
    }

    /**
     * Creates a Report with current version and given extended identity
     */
    public Reporter(String extendedIdentity) {
        this.extendedIdentity = extendedIdentity;
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
                connection.setDoOutput(true);
                connection.setConnectTimeout(10 * 1000);
                connection.setReadTimeout(10 * 1000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("User-Agent", References.ID + "/" + References.VERSION + "@" + extendedIdentity);
                connection.setRequestProperty("Content-length", String.valueOf(reportData.length));
                connection.setRequestProperty("Content-Type", "application/json");

                connection.connect();

                try (OutputStream out = connection.getOutputStream()) {
                    out.write(reportData);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
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

    private Map<String, String> generateBaseReport(LaunchOption option) {
        Map<String, String> report = new HashMap<>();

        report.put("jmccc_name", References.ID);
        report.put("jmccc_version", References.VERSION);
        report.put("os", OsTypes.CURRENT().name());
        report.put("os_version", System.getProperty("os.version"));
        report.put("os_name", System.getProperty("os.name"));
        report.put("os_arch", System.getProperty("os.arch"));
        report.put("java_version", System.getProperty("java.version"));
        report.put("game_version", option.getVersion().getVersion());
        report.put("java_path", option.getEnvironmentOption().getJavaPath().toString());
        report.put("mc_path", option.getEnvironmentOption().getMinecraftDir().toString());
        report.put("max_memory", String.valueOf(option.getMaxMemory()));
        report.put("min_memory", String.valueOf(option.getMinMemory()));

        if (extendedIdentity != null) {
            report.put("ext_id", extendedIdentity);
        }

        return report;
    }

    private Map<String, String> generateSuccessfulReport(LaunchOption option, LaunchResult result) {
        Map<String, String> report = generateBaseReport(option);
        report.put("state", "true");
        return report;
    }

    private Map<String, String> generateUnsuccessfulReport(LaunchOption option, Throwable e) {
        Map<String, String> report = generateBaseReport(option);
        report.put("state", "false");
        report.put("stack_trace", Utils.getStackTrace(e));
        return report;
    }

    private void asyncReport(Runnable runnable) {
        Thread reportThread = new Thread(runnable);
        reportThread.setName("jmccc report thread");
        reportThread.start();
    }

    public void launchSuccessfully(LaunchOption option, LaunchResult result) throws ReportException {
        report(generateSuccessfulReport(option, result));
    }

    public void launchUnsuccessfully(LaunchOption option, Throwable e) throws ReportException {
        report(generateUnsuccessfulReport(option, e));
    }

    public void asyncLaunchSuccessfully(LaunchOption option, LaunchResult result) {
        asyncReport(new Runnable() {

            @Override
            public void run() {
                try {
                    launchSuccessfully(option, result);
                } catch (ReportException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void asyncLaunchUnsuccessfully(LaunchOption option, Throwable e) {
        asyncReport(new Runnable() {

            @Override
            public void run() {
                try {
                    launchUnsuccessfully(option, e);
                } catch (ReportException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
