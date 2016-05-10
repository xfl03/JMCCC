package org.to2mbn.jmccc.exec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

abstract public class ProcessMonitor {

	protected final Process process;

	private Object stateLock = new Object();
	private volatile boolean started = false;

	private ThreadFactory threadFactory;
	private List<Thread> monitors = new ArrayList<>();

	public ProcessMonitor(Process process) {
		this(process, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "process-monitor-" + r);
			}
		});
	}

	public ProcessMonitor(Process process, ThreadFactory threadFactory) {
		this.process = process;
		this.threadFactory = threadFactory;
	}

	abstract protected Collection<? extends Runnable> createMonitors();

	public void start() {
		synchronized (stateLock) {
			if (started) {
				throw new IllegalStateException("already started");
			}

			monitors.clear();
			for (Runnable monitor : createMonitors()) {
				Thread t = threadFactory.newThread(monitor);
				monitors.add(t);
				t.start();
			}

			started = true;
		}
	}

	public void stop() {
		synchronized (stateLock) {
			if (!started) {
				return;
			}

			for (Thread t : monitors) {
				if (t.isAlive()) {
					t.interrupt();
				}
			}
		}
	}

}
