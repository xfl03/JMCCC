package jmccc.cli;

import jmccc.cli.download.ModLoaderDownloader;
import jmccc.cli.download.SimpleDownloader;
import jmccc.cli.launch.SimpleLauncher;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeVersion;
import org.to2mbn.jmccc.version.Version;

public class Main {
    public static void main(String... args) throws Exception {
        String latest = SimpleDownloader.getLatestRelease();
        System.out.println("Latest Release: " + latest);
        ForgeVersion forge = ModLoaderDownloader.getLatestForge(latest);
        System.out.println("Latest Forge: " + forge);
        Version v = SimpleDownloader.download(forge.getVersionName());
//        SimpleLauncher.launch(v);
    }
}
