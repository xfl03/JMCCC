package com.darkyoooooo.jmccc.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.darkyoooooo.jmccc.util.OsTypes;

public class GameProcessMonitor {

    private class LogMonitor implements Runnable {

        /**
         * False for stdout, true for stderr
         */
        private boolean isErr;

        public LogMonitor(boolean isErr) {
            this.isErr = isErr;
        }

        @Override
        public void run() {
            char[] eol = OsTypes.CURRENT().getLineSpearator().toCharArray();
            InputStream in = isErr ? stderr : stdout;
            try (Reader reader = new InputStreamReader(in)) {
                StringBuilder buffer = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    buffer.append((char) ch);

                    // check eol
                    boolean isEOL = true;
                    for (int i = 0; i < eol.length; i++) {
                        if (eol[i] != buffer.charAt(buffer.length() - 1 - i)) {
                            isEOL = false;
                            break;
                        }
                    }

                    if (isEOL) {
                        buffer.delete(buffer.length() - eol.length, buffer.length());
                        String log = buffer.toString();
                        buffer.delete(0, buffer.length());
                        if (isErr) {
                            for (IGameListener l : listeners) {
                                l.onErrorLog(log);
                            }
                        } else {
                            for (IGameListener l : listeners) {
                                l.onLog(log);
                            }
                        }
                    }

                    if (Thread.interrupted()) {
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class ExitMonitor implements Runnable {

        @Override
        public void run() {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            int exitCode = process.exitValue();
            for (IGameListener l : listeners) {
                l.onExit(exitCode);
            }
        }

    }

    private Set<IGameListener> listeners = new HashSet<IGameListener>();

    private Process process;
    private boolean isDaemon;

    private InputStream stdout;
    private InputStream stderr;

    private Thread outThread;
    private Thread errThread;
    private Thread exitThread;

    public GameProcessMonitor(Process process, boolean isDaemon) {
        this.process = process;
        this.isDaemon = isDaemon;
    }

    public GameProcessMonitor(Process process, boolean isDaemon, Set<IGameListener> listeners) {
        this(process, isDaemon);
        this.listeners.addAll(listeners);
    }

    public void addListener(IGameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IGameListener listener) {
        listeners.remove(listener);
    }

    public Set<IGameListener> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }

    public void monitor() {
        stdout = process.getInputStream();
        stderr = process.getErrorStream();

        outThread = new Thread(new LogMonitor(false));
        errThread = new Thread(new LogMonitor(true));
        exitThread = new Thread(new ExitMonitor());

        outThread.setDaemon(isDaemon);
        errThread.setDaemon(isDaemon);
        exitThread.setDaemon(isDaemon);

        outThread.setName("jmccc stdout monitor");
        errThread.setName("jmccc stderr monitor");
        exitThread.setName("jmccc exit monitor");

        outThread.start();
        errThread.start();
        exitThread.start();
    }

    public void shutdown() {
        exitThread.interrupt();

        try {
            stdout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            stderr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outThread.interrupt();
        errThread.interrupt();
    }
}
