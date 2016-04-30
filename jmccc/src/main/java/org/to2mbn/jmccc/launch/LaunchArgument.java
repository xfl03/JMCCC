package org.to2mbn.jmccc.launch;

import static java.util.stream.Collectors.toList;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.util.Platform;
import org.to2mbn.jmccc.version.Version;

/**
 * To generate launching command line.
 */
public class LaunchArgument {

	private LaunchOption launchOption;
	private File nativesPath;
	private Set<File> libraries;
	private Map<String, String> defaultVariables;

	LaunchArgument(LaunchOption launchOption, Map<String, String> defaultVariables, Set<File> libraries, File nativesPath) {
		this.launchOption = launchOption;
		this.libraries = libraries;
		this.nativesPath = nativesPath;
		this.defaultVariables = defaultVariables;
	}

	public String[] generateCommandline() {
		List<String> args = new ArrayList<>();
		Version version = launchOption.getVersion();

		// java path
		args.add(launchOption.getJavaEnvironment().getJavaPath().getAbsolutePath());

		// min memory
		if (launchOption.getMinMemory() != 0) {
			args.add("-Xms" + launchOption.getMinMemory() + "M");
		}

		// max memory
		if (launchOption.getMaxMemory() != 0) {
			args.add("-Xmx" + launchOption.getMaxMemory() + "M");
		}

		// extra jvm arguments
		args.addAll(launchOption.getExtraJvmArguments());

		// natives path
		args.add("-Djava.library.path=" + nativesPath);

		// class path
		// ==========START==========
		args.add("-cp");
		StringBuilder cpBuilder = new StringBuilder();

		// libraries
		for (File lib : libraries) {
			cpBuilder.append(lib.getAbsolutePath()).append(Platform.getPathSpearator());
		}

		args.add(cpBuilder.toString());
		// ==========END==========

		// main class
		args.add(version.getMainClass());

		// template arguments
		args.addAll(getFormattedMinecraftArguments());

		// extra minecraft arguments
		args.addAll(launchOption.getExtraMinecraftArguments());

		// server
		launchOption.getServerInfo().ifPresent(server -> {
			args.add("--server");
			args.add(server.getHost());

			if (server.getPort() > 0) {
				args.add("--port");
				args.add(String.valueOf(server.getPort()));
			}
		});

		// window size settings
		launchOption.getWindowSize().ifPresent(windowSize -> {
			if (windowSize.isFullScreen()) {
				args.add("--fullscreen");
			} else {
				if (windowSize.getHeight() != 0) {
					args.add("--height");
					args.add(String.valueOf(windowSize.getHeight()));
				}
				if (windowSize.getWidth() != 0) {
					args.add("--width");
					args.add(String.valueOf(windowSize.getWidth()));
				}
			}
		});

		return args.toArray(new String[args.size()]);
	}

	private List<String> getFormattedMinecraftArguments() {
		Map<String, String> variables = new HashMap<>();
		variables.putAll(defaultVariables);
		variables.putAll(launchOption.getCommandlineVariables());

		return Arrays.stream(launchOption.getVersion().getLaunchArgs().split(" "))
				.map(argument -> {
					for (Entry<String, String> var : variables.entrySet()) {
						argument = argument.replace("${" + var.getKey() + "}", var.getValue());
					}
					return argument;
				}).collect(toList());
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

	public Map<String, String> getTokens() {
		return defaultVariables;
	}

}
