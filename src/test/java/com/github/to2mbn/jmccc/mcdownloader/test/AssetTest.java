package com.github.to2mbn.jmccc.mcdownloader.test;

import static org.junit.Assert.*;
import org.junit.Test;
import com.github.to2mbn.jmccc.mcdownloader.Asset;

public class AssetTest {

	@Test
	public void testHashPath() {
		Asset asset = new Asset("test/assert_test/test_hash_path", "da39a3ee5e6b4b0d3255bfef95601890afd80709", 0);
		assertEquals("da/da39a3ee5e6b4b0d3255bfef95601890afd80709", asset.getHashPath());
	}

}
