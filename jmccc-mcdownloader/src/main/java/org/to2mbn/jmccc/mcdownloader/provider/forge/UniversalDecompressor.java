package org.to2mbn.jmccc.mcdownloader.provider.forge;

import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class UniversalDecompressor implements ResultProcessor<byte[], Void> {

    private static final String NAME_TEMPLATE = "forge-%s-universal.jar";
    private static final String OLD_NAME_TEMPLATE = "minecraftforge-universal-%s.jar";

    private File target;
    private String[] names;

    public UniversalDecompressor(File target, String m2Version) {
        this.target = target;

        names = new String[]{
                String.format(NAME_TEMPLATE, m2Version),
                String.format(OLD_NAME_TEMPLATE, m2Version)
        };
    }

    @Override
    public Void process(byte[] arg) throws Exception {
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(arg))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (isUniversalJar(entry.getName())) {
                    FileUtils.prepareWrite(target);
                    try (OutputStream out = new FileOutputStream(target)) {
                        byte[] buf = new byte[8192];
                        int read;
                        while ((read = in.read(buf)) != -1) {
                            out.write(buf, 0, read);
                        }
                    }
                    in.closeEntry();
                    return null;
                }

                in.closeEntry();
            }
        }

        throw new IllegalArgumentException("No universal jar found");
    }

    private boolean isUniversalJar(String name) {
        for (String expectedName : names) {
            if (expectedName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

}
