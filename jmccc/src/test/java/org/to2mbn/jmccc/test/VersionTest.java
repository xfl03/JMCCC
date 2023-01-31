package org.to2mbn.jmccc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.to2mbn.jmccc.version.*;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.to2mbn.jmccc.test.TestUtils.*;

@RunWith(Parameterized.class)
public class VersionTest extends MinecraftEnvironmentTest {

    private String versionNumber;
    private Version version;
    private String[] depVersions;
    public VersionTest(String versionNumber, Version version, String[] depVersions) {
        this.versionNumber = versionNumber;
        this.version = version;
        this.depVersions = depVersions;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Map<String, DownloadInfo> emptyDownloads = Collections.emptyMap();
        Object[][] params = new Object[][]{
                {"1.6.4", new Version("1.6.4", "release", "net.minecraft.client.main.Main", "legacy", Arrays.asList("--username ${auth_player_name} --session ${auth_session} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets}".split(" ")), Collections.emptyList(), "1.6.4", hashSet(
                        new Library("net.sf.jopt-simple", "jopt-simple", "4.5"),
                        new Library("org.lwjgl.lwjgl", "lwjgl", "2.9.0"),
                        new Native("org.lwjgl.lwjgl", "lwjgl-platform", "2.9.0", "natives-testplatform", "jar", hashSet("META-INF/"))), true, null, emptyDownloads),
                        null},
                {"16w05b", new Version("16w05b", "snapshot", "net.minecraft.client.main.Main", "1.9", Arrays.asList("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type} --versionType ${version_type}".split(" ")), Collections.emptyList(), "16w05b", hashSet(
                        new Library("com.mojang", "authlib", "1.5.22")), false, new AssetIndexInfo("https://launchermeta.mojang.com/mc-staging/assets/1.9/81f4951679bb400bd031349e278b20b2803dba58/1.9.json", "81f4951679bb400bd031349e278b20b2803dba58", 134402, "1.9", 117237481), hashMap(
                        entry("client", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/client/3a07cea3cf6f1198a7db39a8bd3775883fb391be/client.jar", "3a07cea3cf6f1198a7db39a8bd3775883fb391be", 8692004)),
                        entry("server", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/server/9fdf8a90055b3cf689265cc30bdd9d1faf2c743c/server.jar", "9fdf8a90055b3cf689265cc30bdd9d1faf2c743c", 8843033)))),
                        null},
                {"16w05b_", new Version("16w05b_", "snapshot", "net.minecraft.client.main.Main", "1.9", Arrays.asList("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type} --versionType ${version_type}".split(" ")), Collections.emptyList(), "16w05b_", hashSet(
                        new Library("oshi-project", "oshi-core", "1.1", null, "jar", new LibraryInfo("https://libraries.minecraft.net/oshi-project/oshi-core/1.1/oshi-core-1.1.jar", "9ddf7b048a8d701be231c0f4f95fd986198fd2d8", 30973, "oshi-project/oshi-core/1.1/oshi-core-1.1.jar")),
                        new Native("org.lwjgl.lwjgl", "lwjgl-platform", "2.9.4-nightly-20150209", "natives-testplatform", "jar", new LibraryInfo("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209-natives-linux.jar", "931074f46c795d2f7b30ed6395df5715cfd7675b", 578680, "org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209-natives-linux.jar"), hashSet("META-INF/"))
                ), false, new AssetIndexInfo("https://launchermeta.mojang.com/mc-staging/assets/1.9/81f4951679bb400bd031349e278b20b2803dba58/1.9.json", "81f4951679bb400bd031349e278b20b2803dba58", 134402, "1.9", 117237481), hashMap(
                        entry("client", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/client/3a07cea3cf6f1198a7db39a8bd3775883fb391be/client.jar", "3a07cea3cf6f1198a7db39a8bd3775883fb391be", 8692004)),
                        entry("server", new DownloadInfo("https://launcher.mojang.com/mc/game/16w05b/server/9fdf8a90055b3cf689265cc30bdd9d1faf2c743c/server.jar", "9fdf8a90055b3cf689265cc30bdd9d1faf2c743c", 8843033)))),
                        null},
                {"1.8-forge1.8-11.14.3.1514", new Version("1.8-forge1.8-11.14.3.1514", "release", "net.minecraft.launchwrapper.Launch", "1.8", Arrays.asList("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} --tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker".split(" ")), Collections.emptyList(), "1.8", hashSet(
                        new Library("net.minecraftforge", "forge", "1.8-11.14.3.1514", null, "jar", null, "http://files.minecraftforge.net/maven/", null),
                        new Library("io.netty", "netty-all", "4.0.15.Final"),
                        new Library("com.typesafe.akka", "akka-actor_2.11", "2.3.3", null, "jar", null, "http://files.minecraftforge.net/maven/", new String[]{"ed62e9fc709ca0f2ff1a3220daa8b70a2870078e", "25a86ccfdb6f6dfe08971f4825d0a01be83a6f2e"})),
                        false, null, emptyDownloads),
                        new String[]{"1.8"}
                },
                {"1.7.10-LiteLoader1.7.10", new Version("1.7.10-LiteLoader1.7.10", "release", "net.minecraft.launchwrapper.Launch", "1.7.10", Arrays.asList("--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} --tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker".split(" ")), Collections.emptyList(), "1.7.10", hashSet(
                        new Library("com.mumfrey", "liteloader", "1.7.10", null, "jar", null, "http://dl.liteloader.com/versions/", null),
                        new Library("net.minecraft", "launchwrapper", "1.11"),
                        new Library("org.ow2.asm", "asm-all", "5.0.3"),
                        new Library("com.google.guava", "guava", "16.0"),
                        new Library("com.mojang", "realms", "1.3.5")), false, null, emptyDownloads),
                        new String[]{"1.7.10"}
                }
        };
        return Arrays.asList(params);
    }

    @Override
    protected void copyFiles() throws IOException {
        copyVersionJson(versionNumber);
        if (depVersions != null)
            for (String depVersion : depVersions)
                copyVersionJson(depVersion);

    }

    @Test
    public void testFromJson() throws IOException {
        assertEquals(version, Versions.resolveVersion(mcdir(), versionNumber));
    }

    private void copyVersionJson(String version) throws IOException {
        new File("mcdir/versions/" + version).mkdirs();
        String versionJsonPath = "mcdir/versions/" + version + "/" + version + ".json";
        copyFromJar("/" + versionJsonPath, new File(versionJsonPath));
    }
}
