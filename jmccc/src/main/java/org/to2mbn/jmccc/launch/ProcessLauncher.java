package org.to2mbn.jmccc.launch;

import java.io.IOException;
import org.to2mbn.jmccc.exec.DaemonStreamPumpMonitor;
import org.to2mbn.jmccc.exec.GameProcessListener;
import org.to2mbn.jmccc.exec.LoggingMonitor;
import org.to2mbn.jmccc.exec.ProcessMonitor;

public class ProcessLauncher extends AbstractLauncher {

	private boolean debugPrintCommandline = false;

	/**
	 * Gets whether to print the launch commandline for debugging.
	 * 
	 * @return whether to print the launch commandline for debugging
	 */
	public boolean isDebugPrintCommandline() {
		return debugPrintCommandline;
	}

	/**
	 * Sets whether to print the launch commandline for debugging.
	 * 
	 * @param debugPrintCommandline whether to print the launch commandline for
	 *            debugging.
	 */
	public void setDebugPrintCommandline(boolean debugPrintCommandline) {
		this.debugPrintCommandline = debugPrintCommandline;
	}

	@Override
	protected LaunchResult doLaunch(LaunchArgument arg, GameProcessListener listener) throws LaunchException {
		String[] commandline = arg.generateCommandline();
		if (debugPrintCommandline) {
			commandlineGenerated(commandline);
		}

		ProcessBuilder processBuilder = new ProcessBuilder(commandline);
		processBuilder.directory(arg.getLaunchOption().getRuntimeDirectory().getRoot());

		Process process;
		try {
			process = processBuilder.start();
		} catch (SecurityException | IOException e) {
			throw new LaunchException("Failed to start process", e);
		}

		ProcessMonitor monitor;
		if (listener == null) {
			monitor = new DaemonStreamPumpMonitor(process);
		} else {
			monitor = new LoggingMonitor(process, listener);
		}
		monitor.start();

		return new LaunchResult(monitor, process);
	}

	protected void commandlineGenerated(String[] commandline) {
		if (debugPrintCommandline) {
			printDebugCommandline(commandline);
		}
	}

	private void printDebugCommandline(String[] commandline) {
		StringBuilder sb = new StringBuilder();
		sb.append("jmccc:\n");
		for (String arg : commandline) {
			sb.append(arg).append('\n');
		}
		System.err.println(sb.toString());
	}

}
