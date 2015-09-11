package com.github.to2mbn.jmccc.exec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

abstract public class ProcessMonitor {

    private static final long STOP_TIMEOUT = 2000;

    protected final Process process;

    private Object stateLock = new Object();
    private boolean started = false;

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

    @SuppressWarnings("deprecation")
    public void stop() throws InterruptedException {
        synchronized (stateLock) {
            if (!started) {
                return;
            }

            // interrupt first
            for (Thread t : monitors) {
                if (t.isAlive()) {
                    t.interrupt();
                }
            }

            InterruptedException ex = null;
            long stopTimeout = System.currentTimeMillis() + STOP_TIMEOUT;
            for (Thread t : monitors) {
                if (t.isAlive()) {
                    long now = System.currentTimeMillis();
                    if (ex == null && now < stopTimeout) {
                        // we still have time to wait
                        try {
                            t.join(stopTimeout - now);
                        } catch (InterruptedException e) {
                            ex = e;
                        }
                    }

                    if (t.isAlive()) {
                        // if still running, force stop
                        t.stop();
                    }
                }
            }

            if (ex != null) {
                throw ex;
            }
        }
    }

}
