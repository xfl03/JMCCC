package com.darkyoooooo.jmccc.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
            char[] eol = OsTypes.CURRENT.getLineSpearator().toCharArray();
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
                            listener.onErrorLog(log);
                        } else {
                            listener.onLog(log);
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
            listener.onExit(exitCode);
        }

    }

    private Process process;
    private IGameListener listener;

    private InputStream stdout;
    private InputStream stderr;

    private Thread outThread;
    private Thread errThread;
    private Thread exitThread;

    public GameProcessMonitor(Process process) {
        this.process = process;
    }

    public GameProcessMonitor(Process process, IGameListener listener) {
        this(process);
        this.listener = listener;
    }

    public IGameListener getListener() {
        return listener;
    }

    public void monitor() {
        stdout = process.getInputStream();
        stderr = process.getErrorStream();

        outThread = new Thread(new LogMonitor(false));
        errThread = new Thread(new LogMonitor(true));
        exitThread = new Thread(new ExitMonitor());

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
