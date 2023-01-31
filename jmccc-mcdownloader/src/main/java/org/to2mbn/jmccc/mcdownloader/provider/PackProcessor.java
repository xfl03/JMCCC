package org.to2mbn.jmccc.mcdownloader.provider;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.util.FileUtils;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.to2mbn.jmccc.util.HexUtils.bytesToHex;
import static org.to2mbn.jmccc.util.HexUtils.hexToBytes;

class PackProcessor implements ResultProcessor<byte[], Void> {

    private static final byte[] POSTFIX;

    static {
        POSTFIX = "SIGN".getBytes(StandardCharsets.US_ASCII);
    }

    private final File target;

    public PackProcessor(File target) {
        this.target = target;
    }

    @Override
    public Void process(byte[] data) throws IOException, NoSuchAlgorithmException, CompressorException {
        FileUtils.prepareWrite(target);

        if (data.length < 4 + POSTFIX.length) {
            throw new IOException("pack data too short: " + data.length);
        }
        for (int i = 0; i < POSTFIX.length; i++) {
            if (data[data.length - i - 1] != POSTFIX[POSTFIX.length - i - 1]) {
                throw new IOException("bad postfix");
            }
        }
        int checksumsLengthPos = data.length - POSTFIX.length - 4;
        int checksumsLength = data[checksumsLengthPos] & 0xff | (data[checksumsLengthPos + 1] & 0xff) << 8
                | (data[checksumsLengthPos + 2] & 0xff) << 16 | (data[checksumsLengthPos + 3] & 0xff) << 24;
        if (checksumsLength < 0 || checksumsLength > checksumsLengthPos) {
            throw new IOException("illegal checksums length: " + checksumsLength);
        }
        JarEntry checksumsEntry = new JarEntry("checksums.sha1");
        checksumsEntry.setTime(0);

        byte[] checksumData = new byte[checksumsLength];
        System.arraycopy(data, checksumsLengthPos - checksumsLength, checksumData, 0, checksumsLength);

        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(target.toPath()))) {
            try (CompressorOutputStream pack200Out = new CompressorStreamFactory()
                    .createCompressorOutputStream(CompressorStreamFactory.PACK200, out)) {
                pack200Out.write(data);
                out.putNextEntry(checksumsEntry);
                out.write(checksumData);
                out.closeEntry();
            }
        }

        try {
            verifyChecksums(readChecksums(checksumData));
        } catch (Throwable e) {
            target.delete();
            throw e;
        }

        return null;
    }

    private void verifyChecksums(Map<String, byte[]> checksums) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(target.toPath()))) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                throw new IOException("Entry is null");
            }

            byte[] expectedChecksum = checksums.get(entry.getName());
            if (expectedChecksum != null) {
                sha1.reset();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    sha1.update(buffer, 0, read);
                }
                byte[] actualChecksum = sha1.digest();
                if (!Arrays.equals(expectedChecksum, actualChecksum)) {
                    throw new IOException("illegal sha1 checksum for " + entry.getName() + ": expected="
                            + bytesToHex(expectedChecksum) + ", actual=" + bytesToHex(actualChecksum));
                }
            }

            in.closeEntry();
        }
    }

    private Map<String, byte[]> readChecksums(byte[] checksumData) throws IOException {
        Map<String, byte[]> checksums = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new CharArrayReader(
                new String(checksumData, StandardCharsets.UTF_8).toCharArray()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] splited = line.split(" ", 2);
                    if (splited.length < 2) {
                        throw new IOException("bad checksum format: " + line);
                    }
                    String file = splited[1];
                    byte[] checksum = hexToBytes(splited[0]);
                    checksums.put(file, checksum);
                }
            }
        }
        return checksums;
    }

}
