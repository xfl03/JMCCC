package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.util.Platform;
import org.to2mbn.jmccc.version.Version;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * {@code LaunchArgument} is used to generate launching command line.
 */
public class LaunchArgument {

    private final LaunchOption launchOption;
    private final File nativesPath;
    private final Set<File> libraries;
    private final Map<String, String> defaultVariables;

    public LaunchArgument(LaunchOption launchOption, Map<String, String> defaultVariables, Set<File> libraries, File nativesPath) {
        this.launchOption = launchOption;
        this.libraries = libraries;
        this.nativesPath = nativesPath;
        this.defaultVariables = defaultVariables;
    }

    public List<String> getJvmArguments() {
        List<String> args = new ArrayList<>();

        // min memory
        if (launchOption.getMinMemory() != 0) {
            args.add("-Xms" + launchOption.getMinMemory() + "M");
        }

        // max memory
        if (launchOption.getMaxMemory() != 0) {
            args.add("-Xmx" + launchOption.getMaxMemory() + "M");
        }

        // extra jvm arguments
        for (String arg : launchOption.extraJvmArguments()) {
            if (arg != null) {
                args.add(arg);
            }
        }

        // libraries
        StringBuilder cpBuilder = new StringBuilder();
        for (File lib : libraries) {
            if (lib != null) {
                cpBuilder.append(lib.getAbsolutePath()).append(Platform.getPathSeparator());
            }
        }
        if (cpBuilder.length() > 0) {
            cpBuilder.deleteCharAt(cpBuilder.length() - 1); // to avoid the last unnecessary ':'
        }
        defaultVariables.put("classpath", cpBuilder.toString());

        // JVM arguments
        List<String> jvmArgs = launchOption.getVersion().getJvmArgs();
        if (jvmArgs.isEmpty()) {
            //Default JVM args
            jvmArgs.addAll(Arrays.asList("-Djava.library.path=${natives_directory}", "-cp", "${classpath}"));
        }
        args.addAll(getFormattedMinecraftArguments(jvmArgs));

        return args;
    }

    public List<String> getGameArguments() {

        // template arguments
        List<String> args = new ArrayList<>(getFormattedMinecraftArguments(launchOption.getVersion().getGameArgs()));

        // extra minecraft arguments
        for (String arg : launchOption.extraMinecraftArguments()) {
            if (arg != null) {
                args.add(arg);
            }
        }

        // server
        if (launchOption.getServerInfo() != null && launchOption.getServerInfo().getHost() != null && !launchOption.getServerInfo().getHost().equals("")) {
            args.add("--server");
            args.add(launchOption.getServerInfo().getHost());

            if (launchOption.getServerInfo().getPort() > 0) {
                args.add("--port");
                args.add(String.valueOf(launchOption.getServerInfo().getPort()));
            }
        }

        // window size settings
        if (launchOption.getWindowSize() != null) {
            if (launchOption.getWindowSize().isFullScreen()) {
                args.add("--fullscreen");
            } else {
                if (launchOption.getWindowSize().getHeight() != 0) {
                    args.add("--height");
                    args.add(String.valueOf(launchOption.getWindowSize().getHeight()));
                }
                if (launchOption.getWindowSize().getWidth() != 0) {
                    args.add("--width");
                    args.add(String.valueOf(launchOption.getWindowSize().getWidth()));
                }
            }
        }

        return args;
    }

    public String[] generateCommandline() {
        List<String> args = new ArrayList<>();
        Version version = launchOption.getVersion();

        // java path
        args.add(launchOption.getJavaEnvironment().getJavaPath().getAbsolutePath());

        // jvm arguments
        args.addAll(getJvmArguments());

        // main class
        args.add(version.getMainClass());

        // game arguments
        args.addAll(getGameArguments());

        return args.toArray(new String[0]);
    }

    private List<String> getFormattedMinecraftArguments(List<String> templete) {
        Map<String, String> variables = new HashMap<>();
        variables.putAll(defaultVariables);
        variables.putAll(launchOption.commandlineVariables());

        List<String> args = new ArrayList<>();
        for (String arg : templete) {
            for (Entry<String, String> var : variables.entrySet()) {
                String k = var.getKey();
                String v = var.getValue();
                if (k != null && v != null) {
                    arg = arg.replace("${" + k + "}", v);
                }
            }
            args.add(arg);
        }
        return args;
    }

    // Getters
    public LaunchOption getLaunchOption() {
        return launchOption;
    }

    public File getNativesPath() {
        return nativesPath;
    }

    public Set<File> getLibraries() {
        return libraries;
    }

    public Map<String, String> getTokens() {
        return defaultVariables;
    }
}
