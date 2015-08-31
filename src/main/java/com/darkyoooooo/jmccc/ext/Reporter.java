package com.darkyoooooo.jmccc.ext;

import java.net.HttpURLConnection;
import java.net.URL;

import com.darkyoooooo.jmccc.Jmccc;
import com.darkyoooooo.jmccc.launch.LaunchArgument;
import com.darkyoooooo.jmccc.launch.LaunchResult;
import com.darkyoooooo.jmccc.util.OsTypes;

public class Reporter {
    private static final String version = "2.0-dev";
    private static final String reportLink = "http://darkyoooooo.sinaapp.com/update.php?";
    private static String reportName = "Jmccc@" + version;
    private static boolean disable = false;

    public static void setReportName(String name) {
        reportName = name;
    }
    
    public static void setDisable(boolean flag) {
        disable = flag;
    }

    public static void report(final Jmccc jmccc, final LaunchArgument arg, final LaunchResult result) {
        if(disable) {
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(generateLink(jmccc, arg, result));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10 * 1000);
                    connection.setReadTimeout(10 * 1000);
                    connection.setDoInput(true);
                    connection.setRequestProperty("User-Agent", "Jmccc/" + version);
                    connection.connect();
                    connection.getResponseCode();
                    connection.disconnect();
                } catch (Exception e) {
                }
            }
        });
        thread.setName("Jmccc Report Thread");
        thread.start();
    }

    private static String generateLink(Jmccc jmccc, LaunchArgument arg, LaunchResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(reportLink);
        sb.append("rn=").append(reportName).append('&');
        sb.append("on=").append(System.getProperty("os.name")).append('&');
        sb.append("lr=").append(result.getErrorType().toString()).append('&');
        sb.append("jp=").append(jmccc.getBaseOptions().getJavaPath()).append('&');
        sb.append("gr=").append(jmccc.getBaseOptions().getGameRoot()).append('&');
        sb.append("vn=").append(arg.getLaunchOption().getVersion().getId());
        return resolve(sb.toString());
    }
    
    private static String resolve(String string) {
        return string.replace(OsTypes.CURRENT().getFileSpearator(), "/").replace(" ", "%20").replace("+", "%2B").replace("#", "%23");
    }
}
