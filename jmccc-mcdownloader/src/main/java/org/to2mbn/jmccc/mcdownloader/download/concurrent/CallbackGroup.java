package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Objects;

class CallbackGroup<T> implements Callback<T> {

    private Callback<T>[] callbacks;

    public CallbackGroup(Callback<T>[] callbacks) {
        Objects.requireNonNull(callbacks);
        this.callbacks = callbacks;
    }

    @Override
    public void done(T result) {
        EventDispatchException ex = null;
        for (Callback<T> callback : callbacks) {
            try {
                callback.done(result);
            } catch (Throwable e) {
                if (ex == null) {
                    ex = new EventDispatchException();
                }
                ex.addSuppressed(e);
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

    @Override
    public void failed(Throwable e) {
        EventDispatchException ex1 = null;
        for (Callback<T> callback : callbacks) {
            try {
                callback.failed(e);
            } catch (Throwable e1) {
                if (ex1 == null) {
                    ex1 = new EventDispatchException();
                }
                ex1.addSuppressed(e1);
            }
        }
        if (ex1 != null) {
            throw ex1;
        }
    }

    @Override
    public void cancelled() {
        EventDispatchException ex = null;
        for (Callback<T> callback : callbacks) {
            try {
                callback.cancelled();
            } catch (Throwable e) {
                if (ex == null) {
                    ex = new EventDispatchException();
                }
                ex.addSuppressed(e);
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

}
