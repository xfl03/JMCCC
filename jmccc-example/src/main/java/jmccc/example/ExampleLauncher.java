package jmccc.example;

import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.launch.LaunchException;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.ProcessListener;
import org.to2mbn.jmccc.option.LaunchOption;

import java.io.IOException;

public class ExampleLauncher {
    public static void main(String... args) throws IOException, LaunchException {
        Launcher launcher = LauncherBuilder.create().printDebugCommandline(true).build();
        launcher.launch(new LaunchOption(ExampleConfig.MINECRAFT_VERSION,
                new OfflineAuthenticator(ExampleConfig.PLAYER_NAME),
                ExampleConfig.MINECRAFT_DIRECTORY), new ExampleListener());
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
