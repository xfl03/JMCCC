package org.to2mbn.jmccc.test;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Versions;

public class AssetTest extends MinecraftEnvironmentTest {

	@Override
	protected void copyFiles() throws IOException {
		new File("mcdir/assets/objects/00").mkdirs();
		copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f"));
        copyFromJar("/mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca2f", new File("mcdir/assets/objects/00/000c82756fd54e40cb236199f2b479629d0aca20"));
        new File("mcdir/assets/indexes").mkdirs();
        copyFromJar("/mcdir/assets/indexes/test.json", new File("mcdir/assets/indexes/test.json"));
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

}
