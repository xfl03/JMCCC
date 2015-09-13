package com.github.to2mbn.jmccc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class ChecksumsChecker {

    public boolean checksums(File file, Map<String, String> checksums) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        if (checksums.isEmpty()) {
            return true;
        }

        try (InputStream in = new FileInputStream(file)) {
            return checksums(in, checksums);
        }
    }

    public boolean checksums(InputStream in, Map<String, String> checksums) throws NoSuchAlgorithmException, IOException {
        if (checksums.isEmpty()) {
            return true;
        }

        MessageDigest[] digests = new MessageDigest[checksums.size()];
        byte[][] hashes = new byte[checksums.size()][];
        int i = 0;
        for (Entry<String, String> entry : checksums.entrySet()) {
            digests[i] = MessageDigest.getInstance(entry.getKey());
            hashes[i] = hexToBytes(entry.getValue());
            i++;
        }
        return checksums(in, digests, hashes);
    }

    private boolean checksums(InputStream in, MessageDigest[] digests, byte[][] hashes) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            for (int i = 0; i < digests.length; i++) {
                digests[i].update(buffer, 0, read);
            }
        }
        for (int i = 0; i < digests.length; i++) {
            if (!Arrays.equals(hashes[i], digests[i].digest())) {
                return false;
            }
        }
        return true;
    }

    private byte[] hexToBytes(String hex) {
        char[] chars = hex.toLowerCase().toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        for (int i = 0; i < bytes.length; i++) {
            int pos = i * 2;
            bytes[i] = (byte) ((hexToByte(chars[pos]) << 4) | hexToByte(chars[pos + 1]));
        }
        return bytes;
    }

    private byte hexToByte(char hexChar) {
        if (hexChar <= '9') {
            return (byte) (hexChar - '0');
        }
        return (byte) ((hexChar - 'a') + 10);
    }

}
