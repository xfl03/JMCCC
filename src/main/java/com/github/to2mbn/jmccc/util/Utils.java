package com.github.to2mbn.jmccc.util;

import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class Utils {

    /**
     * Gets the current 'java' path.
     * <p>
     * On *nix systems it's <code>$java.home/bin/java</code>.<br>
     * On Windows it's <code>$java.home\bin\java.exe</code>.
     * 
     * @return the current 'java' path
     */
    public static File getJavaPath() {
        return new File(System.getProperty("java.home"), "bin/java" + (OsTypes.CURRENT == OsTypes.WINDOWS ? ".exe" : ""));
    }

    /**
     * Reads json from given file with charset UTF-8.
     * 
     * @param file file to read
     * @return json in the file
     * @throws IOException an I/O error occurs
     * @throws JSONException a json parsing error occurs
     */
    public static JSONObject readJson(File file) throws IOException, JSONException {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8")) {
            return new JSONObject(new JSONTokener(reader));
        }
    }

    /**
     * Gets the stack trace of the given exception.
     * 
     * @param e the exception
     * @return the stack trace of the given exception
     */
    public static String getStackTrace(Throwable e) {
        CharArrayWriter cw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(cw);
        e.printStackTrace(pw);
        pw.close();
        return cw.toString();
    }

    private Utils() {
    }
}
