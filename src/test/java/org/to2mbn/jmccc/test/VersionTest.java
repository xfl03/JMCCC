package org.to2mbn.jmccc.test;

import static org.junit.Assert.*;
import static org.to2mbn.jmccc.test.TestUtils.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.to2mbn.jmccc.version.AssetIndexDownloadInfo;
import org.to2mbn.jmccc.version.DownloadInfo;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Native;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

@RunWith(Parameterized.class)
public class VersionTest extends MinecraftEnvironmentTest {

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] params = new Object[][] {
				{ "1.6.4", new Version("1.6.4", "release", "net.minecraft.client.main.Main", "legacy", "--username ${auth_player_name} --session ${auth_session} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets}", "1.6.4/1.6.4.jar", hashSet(
						new Library("net.sf.jopt-simple", "jopt-simple", "4.5"),
						new Library("org.lwjgl.lwjgl", "lwjgl", "2.9.0"),
						new Native("org.lwjgl.lwjgl", "lwjgl-platform", "2.9.0", "natives-testplatform", hashSet("META-INF/"), null, null)), true, null, null) },
				{ "16w05b", new Version("16w05b", "snapshot", "net.minecraft.client.main.Main", "1.9", "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type} --versionType ${version_type}", "16w05b/16w05b.jar", hashSet(
						new Library("com.mojang", "authlib", "1.5.22")), false, new AssetIndexDownloadInfo("https://launchermeta.mojang.com/mc-staging/assets/1.9/81f4951679bb400bd031349e278b20b2803dba58/1.9.json", "81f4951679bb400bd031349e278b20b2803dba58", 134402, "1.9", 117237481, true), hashMap(
								entry("client", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/client/3a07cea3cf6f1198a7db39a8bd3775883fb391be/client.jar", "3a07cea3cf6f1198a7db39a8bd3775883fb391be", 8692004)),
								entry("server", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/server/9fdf8a90055b3cf689265cc30bdd9d1faf2c743c/server.jar", "9fdf8a90055b3cf689265cc30bdd9d1faf2c743c", 8843033))
				)) }
		};
		return Arrays.asList(params);
	}

	private String versionNumber;
	private Version version;

	public VersionTest(String versionNumber, Version version) {
		this.versionNumber = versionNumber;
		this.version = version;
	}

	@Override
	protected void copyFiles() throws IOException {
		new File("mcdir/versions/" + versionNumber).mkdirs();
		String versionJsonPath = "mcdir/versions/" + versionNumber + "/" + versionNumber + ".json";
		copyFromJar("/" + versionJsonPath, new File(versionJsonPath));
	}

	@Test
	public void testFromJson() throws IOException {
		assertEquals(version, Versions.resolveVersion(mcdir(), versionNumber));
	}
}
