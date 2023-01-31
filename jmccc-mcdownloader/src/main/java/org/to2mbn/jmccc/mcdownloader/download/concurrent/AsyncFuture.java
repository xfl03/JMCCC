package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncFuture<V> implements Future<V>, Callback<V>, Cancelable {

    private static final int RUNNING = 0;
    private static final int COMPLETING = 1;
    private static final int DONE = 2;
    private static final int FAILED = 3;
    private static final int CANCELLED = 4;

    private final Cancelable cancelable;
    private final AtomicInteger state = new AtomicInteger(RUNNING);
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile Callback<V> callback;
    private volatile Throwable exception;
    private volatile V result;

    public AsyncFuture() {
        this(null);
    }

    public AsyncFuture(Cancelable cancelable) {
        this.cancelable = cancelable;
    }

    public Callback<V> getCallback() {
        return callback;
    }

    public void setCallback(Callback<V> callback) {
        this.callback = callback;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return state.get() == CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state.get() == DONE;
    }

    public boolean isExceptional() {
        int s = state.get();
        return s == FAILED || s == CANCELLED;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if (isRunning()) {
            latch.await();
        }
        return getResult();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(unit);
        if (isRunning()) {
            if (!latch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }
        return getResult();
    }

    @Override
    public void done(V result) {
        if (state.compareAndSet(RUNNING, COMPLETING)) {
            this.result = result;
            state.set(DONE);

            terminated();
            Callback<V> c = callback;
            if (c != null)
                c.done(result);
        }
    }

    @Override
    public void failed(Throwable e) {
        Objects.requireNonNull(e);
        if (state.compareAndSet(RUNNING, COMPLETING)) {
            this.exception = e;
            state.set(FAILED);

            terminated();
            cancelUnderlying();
            Callback<V> c = callback;
            if (c != null)
                c.failed(e);
        } else {

            while (state.get() == COMPLETING)
                Thread.yield();
            if (state.get() == FAILED && this.exception != e) {
                assert exception != null;
                synchronized (exception) {
                    for (Throwable e1 : exception.getSuppressed())
                        if (e1 == e)
                            return;
                    exception.addSuppressed(e);
                }
            }
        }
    }

    @Override
    public void cancelled() {
        if (state.compareAndSet(RUNNING, CANCELLED)) {
            terminated();
            cancelUnderlying();
            Callback<V> c = callback;
            if (c != null)
                c.cancelled();
        }
    }

    private void terminated() {
        latch.countDown();
    }

    private V getResult() throws ExecutionException {
        switch (state.get()) {
            case DONE:
                return result;

            case FAILED:
                throw new ExecutionException(exception);

            case CANCELLED:
                throw new CancellationException();

            default:
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    private void cancelUnderlying() {
        if (cancelable != null)
            cancelable.cancel(true);
    }

    private boolean isRunning() {
        int s = state.get();
        return s == RUNNING || s == COMPLETING;
    }

}
