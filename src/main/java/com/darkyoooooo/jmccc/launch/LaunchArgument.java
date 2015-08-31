package com.darkyoooooo.jmccc.launch;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.version.Version;

/**
 * Used to generate launching command line.
 */
public class LaunchArgument {

    private LaunchOption launchOption;
    private String nativesPath;
    private List<File> libraries;
    private List<String> extendedArguments;
    private Map<String, String> tokens;
    private boolean enableCGC;

    public LaunchArgument(LaunchOption launchOption, Map<String, String> tokens, List<String> extendedArguments, boolean enableCGC, List<File> libraries, String nativesPath) {
        this.launchOption = launchOption;
        this.libraries = libraries;
        this.enableCGC = enableCGC;
        this.nativesPath = nativesPath;
        this.tokens = tokens;
        this.extendedArguments = extendedArguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        OsTypes os = OsTypes.CURRENT();
        Version version = launchOption.getVersion();

        // java pach
        sb.append(launchOption.getEnvironmentOption().getJavaPath()).append(' ');

        // cgc
        if (enableCGC) {
            sb.append("-Xincgc ");
        }

        // min memory
        if (launchOption.getMinMemory() != 0) {
            sb.append("-Xms").append(launchOption.getMinMemory()).append("M ");
        }

        // max memory
        if (launchOption.getMaxMemory() != 0) {
            sb.append("-Xmx").append(launchOption.getMaxMemory()).append("M ");
        }

        // extended arguments
        for (String arg : extendedArguments) {
            sb.append(arg).append(' ');
        }

        // natives path
        if (os == OsTypes.WINDOWS) {
            sb.append("-Djava.library.path=\"" + nativesPath + "\" ");
        } else {
            sb.append("-Djava.library.path=" + nativesPath + " ");
        }

        // class path
        // ==========START==========
        sb.append("-cp \"");

        // libraries
        for (File lib : libraries) {
            sb.append(lib).append(os.getPathSpearator());
        }

        // game jar file
        sb.append(version.getJar()).append(os.getPathSpearator());

        sb.append("\" ");
        // ==========END==========

        // main class
        sb.append(version.getMainClass()).append(' ');

        // templete arguments
        sb.append(getFormattedLaunchArgs()).append(' ');

        // server, port default to 25565
        if (launchOption.getServerInfo() != null && launchOption.getServerInfo().getAddress() != null && !launchOption.getServerInfo().getAddress().equals("")) {
            sb.append("--server ").append(launchOption.getServerInfo().getAddress()).append(' ');
            sb.append("--port ").append(launchOption.getServerInfo().getPort() == 0 ? 25565 : launchOption.getServerInfo().getPort()).append(' ');
        }

        // window size settings
        if (launchOption.getWindowSize() != null) {
            if (launchOption.getWindowSize().isFullSize()) {
                sb.append("--fullscreen").append(' ');
            }
            if (launchOption.getWindowSize().getHeight() > 0) {
                sb.append("--height " + launchOption.getWindowSize().getHeight()).append(' ');
            }
            if (launchOption.getWindowSize().getWidth() > 0) {
                sb.append("--width " + launchOption.getWindowSize().getWidth()).append(' ');
            }
        }

        return sb.toString();
    }

    private String getFormattedLaunchArgs() {
        String arg = launchOption.getVersion().getLaunchArgs();
        for (Entry<String, String> entry : tokens.entrySet()) {
            arg = arg.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return arg;
    }

    public LaunchOption getLaunchOption() {
        return launchOption;
    }

    public String getNativesPath() {
        return nativesPath;
    }

    public List<File> getLibraries() {
        return libraries;
    }

    public List<String> getExtendedArguments() {
        return extendedArguments;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }

    public boolean isEnableCGC() {
        return enableCGC;
    }

}
