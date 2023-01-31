package org.to2mbn.jmccc.test;

import org.junit.After;
import org.junit.Before;
import org.to2mbn.jmccc.option.MinecraftDirectory;

import java.io.*;

abstract public class MinecraftEnvironmentTest {

    abstract protected void copyFiles() throws IOException;

    @Before
    public void setupMinecraftDir() throws IOException {
        cleanupMinecraftDir();
        copyFiles();
    }

    @After
    public void cleanupMinecraftDir() throws IOException {
        delete(new File("mcdir"));
    }

    protected MinecraftDirectory mcdir() {
        return new MinecraftDirectory("mcdir");
    }

    protected void copyFromJar(String jarpath, File target) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(jarpath); OutputStream out = new FileOutputStream(target);) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private void delete(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    delete(child);
                }
            }
            if (!file.delete()) {
                throw new IOException("failed to delete: " + file);
            }
        }
    }

}
