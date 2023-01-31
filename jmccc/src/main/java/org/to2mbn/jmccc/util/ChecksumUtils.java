package org.to2mbn.jmccc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public final class ChecksumUtils {

    private ChecksumUtils() {
    }

    public static byte[] compute(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(algorithm);

        MessageDigest checksum = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            checksum.update(buffer, 0, read);
        }
        return checksum.digest();
    }

    public static byte[] compute(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(algorithm);

        try (InputStream in = new FileInputStream(file)) {
            return compute(in, algorithm);
        }
    }

    public static boolean verify(File file, byte[] checksum, String algorithm, long size) throws IOException, NoSuchAlgorithmException {
        Objects.requireNonNull(file);
        if (checksum != null)
            Objects.requireNonNull(algorithm);

        if (!file.isFile())
            return false;

        if (size != -1 && file.length() != size)
            return false;

        if (checksum != null)
            return Arrays.equals(checksum, compute(file, algorithm));
        else
            return true;
    }

    public static boolean verify(File file, String checksum, String algorithm, long size) throws IOException, NoSuchAlgorithmException {
        return verify(file, checksum == null ? null : HexUtils.hexToBytes(checksum), algorithm, size);
    }

    public static boolean verify(File file, byte[] checksum, String algorithm) throws IOException, NoSuchAlgorithmException {
        return verify(file, checksum, algorithm, -1);
    }

    public static boolean verify(File file, String checksum, String algorithm) throws IOException, NoSuchAlgorithmException {
        return verify(file, checksum, algorithm, -1);
    }
}
