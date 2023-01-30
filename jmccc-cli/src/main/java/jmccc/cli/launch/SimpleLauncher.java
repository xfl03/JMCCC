package jmccc.cli.launch;

import jmccc.cli.Config;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.launch.LaunchException;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.ProcessListener;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.version.Version;

public class SimpleLauncher {

    public static void launch(Version targetVersion) throws LaunchException {
        Launcher launcher = LauncherBuilder.create().printDebugCommandline(true).build();
        LaunchOption option = new LaunchOption(targetVersion,
                new OfflineAuthenticator(Config.PLAYER_NAME),
                Config.MINECRAFT_DIRECTORY);
        //Change Minecraft main menu bottom left text
        option.commandlineVariables().put("version_type", "JMCCC");
        //Set memory to 2048MB
        option.setMaxMemory(2048);
        launcher.launch(option, new ExampleListener());
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
            System.out.println(code);
        }
    }
}
