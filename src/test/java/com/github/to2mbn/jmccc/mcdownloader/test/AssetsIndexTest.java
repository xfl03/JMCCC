package com.github.to2mbn.jmccc.mcdownloader.test;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;
import org.junit.Test;
import com.github.to2mbn.jmccc.mcdownloader.Asset;

public class AssetsIndexTest {

	@Test
	public void testFromJson() {
		String json = "{\"objects\": {\"test1\": {\"hash\": \"10a54fc66c8f479bb65c8d39c3b62265ac82e742\",\"size\": 8112},\"test/test2\": {\"hash\": \"14cfb2f24e7d91dbc22a2a0e3b880d9829320243\",\"size\": 7347},\"test/test3.test\": {\"hash\": \"bf7fadaf64945f6b31c803d086ac6a652aabef9b\",\"size\": 3838}}}";
		Set<Asset> indexAc = Asset.fromJson(new JSONObject(json));
		Set<Asset> assetsEx = new HashSet<>();
		assetsEx.add(new Asset("test1", "10a54fc66c8f479bb65c8d39c3b62265ac82e742", 8112));
		assetsEx.add(new Asset("test/test2", "14cfb2f24e7d91dbc22a2a0e3b880d9829320243", 7347));
		assetsEx.add(new Asset("test/test3.test", "bf7fadaf64945f6b31c803d086ac6a652aabef9b", 3838));
		assertEquals(assetsEx, indexAc);
	}

}
