package jmccc.cli;

import jmccc.cli.download.ModLoaderDownloader;
import jmccc.cli.download.SimpleDownloader;
import jmccc.cli.launch.SimpleLauncher;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;

public class Main {

    /**
     * Arguments:
     * [--player player_name] [--dir minecraft_directory] [version]
     */
    public static void main(String... args) throws Exception {
        //Parse args
        OptionParser parser = new OptionParser();
        OptionSpec<String> playerOption = parser.accepts("player").withOptionalArg().defaultsTo("Player");
        OptionSpec<String> dirOption = parser.accepts("dir").withOptionalArg().defaultsTo(".minecraft");
        OptionSpec<String> versionOption = parser.nonOptions();
        OptionSet options = parser.parse(args);
        String player = playerOption.value(options);
        String dir = dirOption.value(options);
        String version = versionOption.value(options);
        if (version == null) {
            version = "release";
        }
        MinecraftDirectory minecraftDirectory = new MinecraftDirectory(dir);

        //Check special version
        switch (version) {
            case "latest":
            case "release":
                version = SimpleDownloader.getLatestRelease();
                break;
            case "snapshot":
                version = SimpleDownloader.getLatestSnapshot();
                break;
            case "forge":
                version = ModLoaderDownloader.getLatestForge().getVersionName();
                break;
            case "liteloader":
                version = ModLoaderDownloader.getLatestLiteLoader().getVersionName();
                break;
        }
        System.out.println("Version: " + version);

        //Download game if not exist
        if (!minecraftDirectory.getVersionJson(version).exists()) {
            Version v = SimpleDownloader.download(minecraftDirectory, version);
            version = v.getVersion();
        }

        //Launch game
        SimpleLauncher.launch(minecraftDirectory, version, player);
    }
}
