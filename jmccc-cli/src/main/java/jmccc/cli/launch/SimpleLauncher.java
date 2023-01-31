package jmccc.cli.launch;

import jmccc.cli.download.SimpleDownloader;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.MissingDependenciesException;
import org.to2mbn.jmccc.launch.ProcessListener;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Library;


public class SimpleLauncher {

    public static void launch(MinecraftDirectory dir, String targetVersion, String player) throws Exception {
        Launcher launcher = LauncherBuilder.create().printDebugCommandline(true).build();
        LaunchOption option = new LaunchOption(targetVersion, new OfflineAuthenticator(player), dir);
        //Change Minecraft main menu bottom left text
        option.commandlineVariables().put("version_type", "JMCCC 3.0");
        //Set memory to 2048MB
        option.setMaxMemory(2048);
        ProcessListener listener = new ExampleListener();
        try {
            launcher.launch(option, listener);
        } catch (MissingDependenciesException e) {
            for (Library lib : e.getMissingLibraries()) {
                SimpleDownloader.downloadLibrary(dir, lib);
            }
            launcher.launch(option, listener);
        }
    }

    private static class ExampleListener implements ProcessListener {

        @Override
        public void onLog(String log) {
            System.out.println(log);
        }

        @Override
        public void onErrorLog(String log) {
            System.err.println(log);
        }

        @Override
        public void onExit(int code) {
            System.out.println("Game exited with " + code);
            SimpleDownloader.downloader.shutdown();
            System.exit(0);
        }
    }
}
