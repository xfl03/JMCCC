package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class CallbackAsyncTask<V> implements RunnableFuture<V>, Cancelable {

    private final Set<Object> cancelables = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
    private final AsyncFuture<V> future;
    private final Callback<V> lifecycle;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CallbackAsyncTask() {
        future = new AsyncFuture<>(new CancelProcesser());
        lifecycle = new InterruptedExceptionMapper<V>(future);
    }

    private static void cancelCancelable(Object cancelable, boolean mayInterruptIfRunning) {
        if (cancelable instanceof Future) {
            ((Future<?>) cancelable).cancel(mayInterruptIfRunning);
        } else if (cancelable instanceof Cancelable) {
            ((Cancelable) cancelable).cancel(mayInterruptIfRunning);
        }
    }

    public Callback<V> getCallback() {
        return future.getCallback();
    }

    public void setCallback(Callback<V> callback) {
        future.setCallback(callback);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    public boolean isExceptional() {
        return future.isExceptional();
    }

    @Override
    public void run() {
        if (!future.isCancelled() && running.compareAndSet(false, true)) {
            Cancelable canceller = new ThreadCancelableAdapter(Thread.currentThread());
            addCancelable(canceller);
            try {
                if (Thread.interrupted())
                    return;

                try {
                    execute();
                } catch (Throwable e) {
                    lifecycle.failed(e);
                }
            } finally {
                removeCancelable(canceller);
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    abstract protected void execute() throws Exception;

    protected Callback<V> lifecycle() {
        return lifecycle;
    }

    protected void addCancelable(Cancelable cancelable) {
        cancelables.add(cancelable);
        cancelIfNecessary(cancelable);
    }

    protected void addCancelable(Future<?> cancelable) {
        cancelables.add(cancelable);
        cancelIfNecessary(cancelable);
    }

    protected void removeCancelable(Cancelable cancelable) {
        cancelables.remove(cancelable);
    }

    protected void removeCancelable(Future<?> cancelable) {
        cancelables.remove(cancelable);
    }

    protected <R> FutureManager<R> createFutureManager() {
        return new FutureManagerImpl<R>();
    }

    private void cancelIfNecessary(Object cancelable) {
        if (future.isCancelled()) {
            cancelCancelable(cancelable, true);
            cancelables.remove(cancelable);
        }
    }

    public static interface FutureManager<T> extends Callback<T> {

        void setFuture(Future<?> future);

    }

    private static class InterruptedExceptionMapper<V> implements Callback<V> {

        private final Callback<V> mapped;

        public InterruptedExceptionMapper(Callback<V> mapped) {
            this.mapped = mapped;
        }

        @Override
        public void done(V result) {
            mapped.done(result);
        }

        @Override
        public void failed(Throwable e) {
            if (e instanceof InterruptedException) {
                mapped.cancelled();
            } else {
                mapped.failed(e);
            }
        }

        @Override
        public void cancelled() {
            mapped.cancelled();
        }

    }

    private static class ThreadCancelableAdapter implements Cancelable {

        private final Thread t;

        public ThreadCancelableAdapter(Thread t) {
            this.t = t;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            t.interrupt();
            return true;
        }

    }

    private class CancelProcesser implements Cancelable {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            for (Object cancelable : cancelables) {
                cancelCancelable(cancelable, mayInterruptIfRunning);
                cancelables.remove(cancelable);
            }
            return true;
        }

    }

    private class FutureManagerImpl<R> implements FutureManager<R> {

        private final Object lock = new Object();
        private volatile Future<?> subfuture;
        private volatile boolean terminated = false;

        @Override
        public void done(R result) {
            removeFuture();
        }

        @Override
        public void failed(Throwable e) {
            removeFuture();
        }

        @Override
        public void cancelled() {
            removeFuture();
        }

        @Override
        public void setFuture(Future<?> subfuture) {
            Objects.requireNonNull(subfuture);
            synchronized (lock) {
                if (!terminated) {
                    this.subfuture = subfuture;
                    addCancelable(subfuture);
                }
            }
        }

        private void removeFuture() {
            synchronized (lock) {
                terminated = true;
                if (subfuture != null) {
                    removeCancelable(subfuture);
                    subfuture = null;
                }
            }
        }

    }

}
