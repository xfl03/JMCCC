package org.to2mbn.jmccc.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.Platform;
import org.to2mbn.jmccc.version.Version;

/**
 * To generate launching command line.
 */
class LaunchArgument {

	private LaunchOption launchOption;
	private File nativesPath;
	private Set<File> libraries;
	private Map<String, String> defaultVariables;

	public LaunchArgument(LaunchOption launchOption, Map<String, String> defaultVariables, Set<File> libraries, File nativesPath) {
		this.launchOption = launchOption;
		this.libraries = libraries;
		this.nativesPath = nativesPath;
		this.defaultVariables = defaultVariables;
	}

	public String[] generateCommandline() {
		List<String> args = new ArrayList<>();
		Version version = launchOption.getVersion();
		MinecraftDirectory mcdir = launchOption.getMinecraftDirectory();

		// java path
		args.add(launchOption.getJavaOption().getJavaPath().toString());

		// cgc
		if (launchOption.getJavaOption().isInCGC()) {
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

		// extra jvm arguments
		if (launchOption.getExtraJvmArguments() != null) {
			for (String arg : launchOption.getExtraJvmArguments()) {
				if (arg != null) {
					args.add(arg);
				}
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
			cpBuilder.append(lib).append(Platform.getPathSpearator());
		}

		// game jar file
		cpBuilder.append(mcdir.getVersionJar(version)).append(Platform.getPathSpearator());

		args.add(cpBuilder.toString());
		// ==========END==========

		// main class
		args.add(version.getMainClass());

		// template arguments
		args.addAll(getFormattedMinecraftArguments());

		// extra minecraft arguments
		if (launchOption.getExtraMinecraftArguments() != null) {
			for (String arg : launchOption.getExtraMinecraftArguments()) {
				if (arg != null) {
					args.add(arg);
				}
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

		return args.toArray(new String[args.size()]);
	}

	private List<String> getFormattedMinecraftArguments() {
		Map<String, String> variables = new HashMap<>();
		variables.putAll(defaultVariables);
		Map<String, String> customizedVariables = launchOption.getCommandlineVariables();
		if (customizedVariables != null)
			variables.putAll(customizedVariables);

		String templete = launchOption.getVersion().getLaunchArgs();
		List<String> args = new ArrayList<>();
		for (String arg : templete.split(" ")) {
			for (Entry<String, String> var : variables.entrySet()) {
				arg = arg.replace("${" + var.getKey() + "}", var.getValue());
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

	public Map<String, String> getTokens() {
		return defaultVariables;
	}

}
