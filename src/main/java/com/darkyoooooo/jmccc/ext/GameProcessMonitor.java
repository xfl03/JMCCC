package com.darkyoooooo.jmccc.ext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.darkyoooooo.jmccc.util.OsTypes;

public final class GameProcessMonitor {
    private static final GameProcessMonitor instance = new GameProcessMonitor();
    
    public static GameProcessMonitor instance() {
        return instance;
    }

    private final List<IGameListener> listeners;

    public GameProcessMonitor() {
        this.listeners = new ArrayList<IGameListener>(5);
    }

    public void addListener(IGameListener listener) {
        listeners.add(listener);
    }

    public void monitor(final Process process) {
        Thread logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStreamReader reader = new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
                    int i;
                    char[] buffer = new char[1024];
                    while ((i = reader.read(buffer)) != -1) {
                        String log = new String(buffer, 0, i).intern().replace(OsTypes.CURRENT().getLineSpearator(), "");
                        for (IGameListener listener : listeners) {
                            listener.onLog(log);
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                }
            }
        });
        Thread errorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStreamReader reader = new InputStreamReader(process.getErrorStream(), Charset.defaultCharset());
                    int i;
                    char[] buffer = new char[1024];
                    while ((i = reader.read(buffer)) != -1) {
                        String log = new String(buffer, 0, i).intern().replace(OsTypes.CURRENT().getLineSpearator(), "");
                        for (IGameListener listener : listeners) {
                            listener.onErrorLog(log);
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                }
            }
        });
        Thread exitCodeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    process.waitFor();
                    int exitCode = process.exitValue();
                    for (IGameListener listener : listeners) {
                        listener.onExit(exitCode);
                    }
                } catch (Exception e) {
                }
            }
        });
        logThread.setName("Jmccc Monitor Thread (Log)");
        logThread.start();
        errorThread.setName("Jmccc Monitor Thread (ErrorLog)");
        errorThread.start();
        exitCodeThread.setName("Jmccc Monitor Thread (ExitCode)");
        exitCodeThread.start();
    }
}
