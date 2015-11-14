package com.github.to2mbn.jmccc.test;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Asset;

public class AssetTest {

	@Before
	public void setupMinecraftDir() throws IOException {
		cleanupMinecraftDir();
		new File("mcdir/assets/objects/00").mkdirs();
		copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f"));
		copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca20"));
	}

	@After
	public void cleanupMinecraftDir() throws IOException {
		delete(new File("mcdir"));
	}

	@Test
	public void testHashCheckOk() throws IOException, GeneralSecurityException {
		assertEquals(true, new Asset("minecraft/sounds/mob/skeleton/step3.ogg", "000c82756fd54e40cb236199f2b479629d0aca2f", 8565).isValid(mcdir()));
	}

	@Test
	public void testHashCheckFailSize() throws IOException, GeneralSecurityException {
		assertEquals(false, new Asset("minecraft/sounds/mob/skeleton/step3.ogg", "000c82756fd54e40cb236199f2b479629d0aca2f", 2333).isValid(mcdir()));
	}

	@Test
	public void testHashCheckFailHash() throws IOException, GeneralSecurityException {
		assertEquals(false, new Asset("minecraft/sounds/mob/skeleton/step3.ogg", "000c82756fd54e40cb236199f2b479629d0aca20", 8565).isValid(mcdir()));
	}

	private MinecraftDirectory mcdir() {
		return new MinecraftDirectory("mcdir");
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

	private void copyFromJar(String jarpath, File target) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(jarpath); OutputStream out = new FileOutputStream(target);) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		}
	}

}
