package jmccc.cli.launch;

import jmccc.cli.Config;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.ProcessListener;
import org.to2mbn.jmccc.option.LaunchOption;


public class SimpleLauncher {

    public static void launch(String targetVersion) throws Exception {
        Launcher launcher = LauncherBuilder.create().printDebugCommandline(true).build();
        LaunchOption option = new LaunchOption(targetVersion,
                new OfflineAuthenticator(Config.PLAYER_NAME),
                Config.MINECRAFT_DIRECTORY);
        //Change Minecraft main menu bottom left text
        option.commandlineVariables().put("version_type", "JMCCC 3.0");
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
