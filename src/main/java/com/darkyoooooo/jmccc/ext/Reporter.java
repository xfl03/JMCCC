package com.darkyoooooo.jmccc.ext;

import java.net.HttpURLConnection;
import java.net.URL;

import com.darkyoooooo.jmccc.Jmccc;
import com.darkyoooooo.jmccc.launch.LaunchArgument;

public class Reporter {
	private static final String version = "1.3";
	private static final String reportLink = "http://darkyoooooo.sinaapp.com/update.php?";
	private static String reportName = "Jmccc@" + version;
	
	public static void setReportName(String name) {
		reportName = handleString(name);
	}
	
	public static void report(Jmccc jmccc, LaunchArgument arg) throws Exception {
		URL url = new URL(generateArgs(jmccc, arg));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10 * 1000);
		connection.setReadTimeout(10 * 1000);
		connection.setDoInput(true);
		connection.setRequestProperty("User-Agent", "Jmccc/" + version);
		connection.connect();
		connection.getResponseCode();
		connection.disconnect();
	}
	
	private static String generateArgs(Jmccc jmccc, LaunchArgument arg) {
		StringBuilder sb = new StringBuilder();
		sb.append(reportLink);
		sb.append("rn=").append(reportName).append('&');
		sb.append("on=").append(handleString(System.getProperty("os.name"))).append('&');
		sb.append("jp=").append(handleString(jmccc.getBaseOptions().getJavaPath())).append('&');
		sb.append("gr=").append(handleString(jmccc.getBaseOptions().getGameRoot())).append('&');
		sb.append("vn=").append(handleString(arg.getLaunchOption().getVersion().getId()));
		String s = sb.toString();
		return s;
	}
	
	private static String handleString(String string) {
		return string.replace(" ", "_").replace('\\', '/');
	}
}
