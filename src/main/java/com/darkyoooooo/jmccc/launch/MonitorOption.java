package com.darkyoooooo.jmccc.launch;

import java.util.Collections;
import java.util.Set;
import com.darkyoooooo.jmccc.ext.IGameListener;

public class MonitorOption {

    private boolean daemon;
    private Set<IGameListener> listeners;

    public MonitorOption(boolean daemon, IGameListener listener) {
        this(daemon, Collections.singleton(listener));
    }

    public MonitorOption(boolean daemon, Set<IGameListener> listeners) {
        this.daemon = daemon;
        this.listeners = listeners;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public Set<IGameListener> getListeners() {
        return listeners;
    }

    public void setListeners(Set<IGameListener> listeners) {
        this.listeners = listeners;
    }

}
