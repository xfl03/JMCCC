package org.to2mbn.jmccc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public final class FileUtils {

    private FileUtils() {
    }

    public static void mkdirs(File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Cannot mkdirs: " + dir);
        }
    }

    public static void prepareWrite(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            mkdirs(parent);
        }
    }

    public static void copyFile(File src, File target) throws IOException {
        prepareWrite(target);
        try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(target)) {
            FileChannel chin = in.getChannel();
            FileChannel chout = out.getChannel();
            chin.transferTo(0, chin.size(), chout);
        }
    }
}
