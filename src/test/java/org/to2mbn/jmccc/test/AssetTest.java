package org.to2mbn.jmccc.test;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Versions;

public class AssetTest {

	@Before
	public void setupMinecraftDir() throws IOException {
		cleanupMinecraftDir();
		new File("mcdir/assets/objects/00").mkdirs();
		copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f"));
        copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca20"));
        new File("mcdir/assets/indexes").mkdirs();
        copyFromJar("/mcdir/assets/indexes/test.json", new File("mcdir/assets/indexes/test.json"));
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

    @Test
    public void testFromJson() throws IOException {
        Set<Asset> indexAc = Versions.resolveAssets(mcdir(), "test");
        Set<Asset> assetsEx = new HashSet<>();
        assetsEx.add(new Asset("test1", "10a54fc66c8f479bb65c8d39c3b62265ac82e742", 8112));
        assetsEx.add(new Asset("test/test2", "14cfb2f24e7d91dbc22a2a0e3b880d9829320243", 7347));
        assetsEx.add(new Asset("test/test3.test", "bf7fadaf64945f6b31c803d086ac6a652aabef9b", 3838));
        assertEquals(assetsEx, indexAc);
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
