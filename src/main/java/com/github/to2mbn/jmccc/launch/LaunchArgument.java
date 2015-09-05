package com.github.to2mbn.jmccc.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.github.to2mbn.jmccc.option.LaunchOption;
import com.github.to2mbn.jmccc.util.OsTypes;
import com.github.to2mbn.jmccc.version.Version;

/**
 * Used to generate launching command line.
 */
class LaunchArgument {

    private LaunchOption launchOption;
    private File nativesPath;
    private Set<File> libraries;
    private List<String> extendedArguments;
    private Map<String, String> tokens;
    private boolean enableCGC;

    public LaunchArgument(LaunchOption launchOption, Map<String, String> tokens, List<String> extendedArguments, boolean enableCGC, Set<File> libraries, File nativesPath) {
        this.launchOption = launchOption;
        this.libraries = libraries;
        this.enableCGC = enableCGC;
        this.nativesPath = nativesPath;
        this.tokens = tokens;
        this.extendedArguments = extendedArguments;
    }

    public String[] generateCommandline() {
        List<String> args = new ArrayList<>();
        OsTypes os = OsTypes.CURRENT;
        Version version = launchOption.getVersion();

        // java pach
        args.add(launchOption.getEnvironmentOption().getJavaPath().toString());

        // cgc
        if (enableCGC) {
            args.add("-Xincgc");
        }

        // min memory
        if (launchOption.getMinMemory() != 0) {
            args.add("-Xms" + launchOption.getMinMemory() + "M");
        }

        // max memory
        if (launchOption.getMaxMemory() != 0) {
            args.add("-Xmx" + launchOption.getMaxMemory() + "M");
        }

        // extended arguments
        if (extendedArguments != null) {
            for (String arg : extendedArguments) {
                args.add(arg);
            }
        }

        // natives path
        args.add("-Djava.library.path=" + nativesPath);

        // class path
        // ==========START==========
        args.add("-cp");
        StringBuilder cpBuilder = new StringBuilder();

        // libraries
        for (File lib : libraries) {
            cpBuilder.append(lib).append(os.getPathSpearator());
        }

        // game jar file
        cpBuilder.append(version.getJar()).append(os.getPathSpearator());

        args.add(cpBuilder.toString());
        // ==========END==========

        // main class
        args.add(version.getMainClass());

        // templete arguments
        args.addAll(getFormattedTokens());

        // server
        if (launchOption.getServerInfo() != null && launchOption.getServerInfo().getAddress() != null && !launchOption.getServerInfo().getAddress().equals("")) {
            args.add("--server");
            args.add(launchOption.getServerInfo().getAddress());

            if (launchOption.getServerInfo().getPort() == 0) {
                args.add("--port");
                args.add(String.valueOf(launchOption.getServerInfo().getPort()));
            }
        }

        // window size settings
        if (launchOption.getWindowSize() != null) {
            if (launchOption.getWindowSize().isFullSize()) {
                args.add("--fullscreen");
            }
            if (launchOption.getWindowSize().getHeight() != 0) {
                args.add("--height");
                args.add(String.valueOf(launchOption.getWindowSize().getHeight()));
            }
            if (launchOption.getWindowSize().getWidth() != 0) {
                args.add("--width");
                args.add(String.valueOf(launchOption.getWindowSize().getWidth()));
            }
        }

        return args.toArray(new String[args.size()]);
    }

    private List<String> getFormattedTokens() {
        String templete = launchOption.getVersion().getLaunchArgs();
        List<String> args = new ArrayList<>();
        for (String arg : templete.split(" ")) {
            for (Entry<String, String> token : tokens.entrySet()) {
                arg = arg.replace("${" + token.getKey() + "}", token.getValue());
            }
            args.add(arg);
        }
        return args;
    }

    public LaunchOption getLaunchOption() {
        return launchOption;
    }

    public File getNativesPath() {
        return nativesPath;
    }

    public Set<File> getLibraries() {
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
