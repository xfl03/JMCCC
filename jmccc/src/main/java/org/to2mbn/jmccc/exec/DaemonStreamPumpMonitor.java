package org.to2mbn.jmccc.exec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;

public class DaemonStreamPumpMonitor extends ProcessMonitor {

	private static class StreamPump implements Runnable {

		private InputStream in;
		private boolean isErr;

		public StreamPump(InputStream in, boolean isErr) {
			this.in = in;
			this.isErr = isErr;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					if (in.read() == -1) {
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		@Override
		public String toString() {
			return isErr ? "stderr" : "stdout";
		}

	}

	public DaemonStreamPumpMonitor(Process process) {
		super(process, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "process-daemon-monitor-" + r);
				t.setDaemon(true);
				return t;
			}
		});
	}

	@Override
	protected Collection<? extends Runnable> createMonitors() {
		return Arrays.asList(new StreamPump(process.getErrorStream(), true), new StreamPump(process.getInputStream(), false));
	}

}
