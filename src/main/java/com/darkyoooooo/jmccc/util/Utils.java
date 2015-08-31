package com.darkyoooooo.jmccc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.darkyoooooo.jmccc.launch.Jmccc;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;

public class Utils {
    public static List<String> resolveRealLibPaths(Jmccc jmccc, List<Library> list) {
        List<String> realPaths = new ArrayList<String>();
        for (Library lib : list) {
            String path = Utils.handlePath(String.format("%s/libraries/%s/%s/%s/%s-%s.jar", jmccc.getBaseOptions().getGameRoot(), lib.getDomain().replace(".", "/"), lib.getName(), lib.getVersion(),
                    lib.getName(), lib.getVersion()));
            realPaths.add(path);
            if (!new File(path).exists()) {
                Jmccc.MISSING_LIBRARIES.add(lib);
            }
        }
        return realPaths;
    }

    public static List<String> resolveRealNativePaths(Jmccc jmccc, List<Native> list) {
        List<String> realPaths = new ArrayList<String>();
        for (Native nat : list) {
            if (!nat.isAllowed()) {
                continue;
            }
            String path = Utils.handlePath(String.format("%s/libraries/%s/%s/%s/%s-%s-%s.jar", jmccc.getBaseOptions().getGameRoot(), nat.getDomain().replace(".", "/"), nat.getName(),
                    nat.getVersion(), nat.getName(), nat.getVersion(), nat.getSuffix()));
            realPaths.add(path);
            if (!new File(path).exists()) {
                Jmccc.MISSING_NATIVES.add(nat);
            }
        }
        return realPaths;
    }

    public static String handlePath(String path) {
        return new File(path).getAbsolutePath();
    }

    public static String getJavaPath() {
        return handlePath(System.getProperty("java.home") + "/bin/java" + (OsTypes.CURRENT() == OsTypes.WINDOWS ? ".exe" : ""));
    }

    public static String genRandomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String readFileToString(File file) throws IOException {
        StringBuffer buffer = new StringBuffer();
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        while (reader.ready()) {
            buffer.append(reader.readLine()).append(OsTypes.CURRENT().getLineSpearator());
        }
        fileReader.close();
        reader.close();
        return buffer.toString();
    }

    public static void uncompressZipFile(File file, String outputPath) throws IOException {
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
        BufferedInputStream input = new BufferedInputStream(zipInput);
        OutputStream output = null;
        File temp = null;
        ZipEntry entry = null;
        while ((entry = zipInput.getNextEntry()) != null) {
            temp = new File(outputPath, entry.getName());
            if (entry.isDirectory()) {
                temp.mkdir();
            } else {
                if (!temp.exists()) {
                    new File(temp.getParent()).mkdirs();
                }
                output = new FileOutputStream(temp);
                int i;
                byte[] b = new byte[1024];
                while ((i = input.read(b)) != -1) {
                    output.write(b, 0, i);
                }
            }
        }
        zipInput.close();
        input.close();
        output.close();
    }

    public static boolean isCGCSupported() {
        String javaVersion = System.getProperty("java.version");
        return !(javaVersion.contains("1.8.") || javaVersion.contains("1.9."));
    }
}
