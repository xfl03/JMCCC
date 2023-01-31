package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.*;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class CombinedDownloaderImpl implements CombinedDownloader {

    private final ReadWriteLock globalRwlock = new ReentrantReadWriteLock();
    private final Set<Future<?>> tasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());
    private ExecutorService executor;
    private Downloader downloader;
    private int defaultTries;

    private volatile boolean shutdown;
    public CombinedDownloaderImpl(ExecutorService executor, Downloader downloader, int defaultTries) {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(downloader);
        if (defaultTries < 1)
            throw new IllegalArgumentException(String.valueOf(defaultTries));

        this.executor = executor;
        this.downloader = downloader;
        this.defaultTries = defaultTries;
    }

    @Override
    public <T> Future<T> download(CombinedDownloadTask<T> downloadTask, CombinedDownloadCallback<T> callback, int tries) {
        Objects.requireNonNull(downloadTask);
        if (tries < 1)
            throw new IllegalArgumentException("tries < 1");

        CombinedAsyncTask<T> task = new CombinedAsyncTask<>(downloadTask, callback == null ? CombinedDownloadCallbacks.<T>empty() : callback, tries);
        Callback<T> statusCallback = Callbacks.whatever(new TaskInactiver(task));
        if (callback != null) {
            statusCallback = Callbacks.group(statusCallback, callback);
        }
        task.setCallback(callback);

        Lock lock = globalRwlock.readLock();
        lock.lock();
        try {
            ensureRunning();

            tasks.add(task);
            executor.execute(task);
        } finally {
            lock.unlock();
        }

        return task;
    }

    @Override
    public void shutdown() {
        Lock lock = globalRwlock.writeLock();
        lock.lock();
        try {
            if (shutdown) {
                return;
            }

            shutdown = true;
        } finally {
            lock.unlock();
        }

        for (Future<?> task : tasks)
            task.cancel(true);

        executor.shutdownNow();
        downloader.shutdown();
        executor = null;
        downloader = null;
    }

    @Override
    public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback) {
        return download(task, callback, defaultTries);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
        return download(task, callback, defaultTries);
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
        Objects.requireNonNull(task);
        if (tries < 1)
            throw new IllegalArgumentException("tries < 1");

        Lock lock = globalRwlock.readLock();
        lock.lock();
        try {
            ensureRunning();

            return downloader.download(task, callback, tries);
        } finally {
            lock.unlock();
        }
    }

    private void ensureRunning() {
        if (shutdown)
            throw new RejectedExecutionException("The downloader has been shutdown.");
    }

    @Override
    public String toString() {
        return String.format("CombinedDownloaderImpl [executor=%s, downloader=%s, defaultTries=%s, shutdown=%s]", executor, downloader, defaultTries, shutdown);
    }

    private class CombinedAsyncTask<T> extends CallbackAsyncTask<T> implements CombinedDownloadContext<T> {

        private final CombinedDownloadTask<T> task;
        private final CombinedDownloadCallback<T> callback;
        private final int tries;
        private final SubtaskCountdownAction countdownAction = new SubtaskCountdownAction();
        private final SubtaskCounter subtaskCounter = new SubtaskCounter();

        public CombinedAsyncTask(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
            Objects.requireNonNull(task);
            Objects.requireNonNull(callback);
            if (tries < 1)
                throw new IllegalArgumentException(String.valueOf(tries));

            this.task = task;
            this.callback = callback;
            this.tries = tries;
        }

        @Override
        public <R> Future<R> submit(Callable<R> task, Callback<R> injectedCallback, boolean fatal) throws InterruptedException {
            Objects.requireNonNull(task);

            CallbackFutureTask<R> futureTask = new CallbackFutureTask<>(task);
            List<Callback<R>> callbacks = new ArrayList<>();

            FutureManager<R> futureManager = createFutureManager();
            futureManager.setFuture(futureTask);
            callbacks.add(wrapCallback(futureManager));

            if (injectedCallback != null)
                callbacks.add(wrapCallback(injectedCallback));

            if (fatal)
                callbacks.add(wrapCallback(new FatalSubtaskCallback<R>()));

            callbacks.add(wrapCallback(Callbacks.<R>whatever(countdownAction)));

            futureTask.setCallback(Callbacks.group(callbacks));

            Lock lock = globalRwlock.readLock();
            lock.lock();
            try {
                checkInterrupted();

                subtaskCounter.countUp();
                executor.execute(futureTask);
            } finally {
                lock.unlock();
            }

            return futureTask;
        }

        @Override
        public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> injectedCallback, boolean fatal) throws InterruptedException {
            Objects.requireNonNull(task);

            List<DownloadCallback<R>> callbacks = new ArrayList<>();

            FutureManager<R> futureManager = createFutureManager();
            callbacks.add(wrapDownloadCallback(DownloadCallbacks.fromCallback(futureManager)));

            if (injectedCallback != null)
                callbacks.add(wrapDownloadCallback(injectedCallback));

            DownloadCallback<R> foreignCallback = callback.taskStart(task);
            if (foreignCallback != null)
                callbacks.add(wrapDownloadCallback(foreignCallback));

            if (fatal)
                callbacks.add(wrapDownloadCallback(DownloadCallbacks.fromCallback(new FatalSubtaskCallback<R>())));

            callbacks.add(wrapDownloadCallback(DownloadCallbacks.<R>whatever(countdownAction)));

            Future<R> future;

            Lock lock = globalRwlock.readLock();
            lock.lock();
            try {
                checkInterrupted();

                subtaskCounter.countUp();
                future = downloader.download(task, DownloadCallbacks.group(callbacks), tries);
                futureManager.setFuture(future);
            } finally {
                lock.unlock();
            }

            return future;
        }

        @Override
        public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> injectedCallback, boolean fatal) throws InterruptedException {
            Objects.requireNonNull(task);

            List<CombinedDownloadCallback<R>> callbacks = new ArrayList<>();

            FutureManager<R> futureManager = createFutureManager();
            callbacks.add(wrapCombinedDownloadCallback(CombinedDownloadCallbacks.fromCallback(futureManager)));

            if (injectedCallback != null)
                callbacks.add(wrapCombinedDownloadCallback(injectedCallback));

            callbacks.add(wrapCombinedDownloadCallback(new SubDownloadTaskMapper<R>()));

            if (fatal)
                callbacks.add(wrapCombinedDownloadCallback(CombinedDownloadCallbacks.fromCallback(new FatalSubtaskCallback<R>())));

            callbacks.add(wrapCombinedDownloadCallback(CombinedDownloadCallbacks.<R>whatever(countdownAction)));

            Future<R> future;

            Lock lock = globalRwlock.readLock();
            lock.lock();
            try {
                checkInterrupted();

                subtaskCounter.countUp();
                future = CombinedDownloaderImpl.this.download(task, CombinedDownloadCallbacks.group(callbacks), tries);
                futureManager.setFuture(future);
            } finally {
                lock.unlock();
            }

            return future;
        }

        @Override
        public void awaitAllTasks(Callable<Void> callback) throws InterruptedException {
            checkInterrupted();
            subtaskCounter.awaitAllTasks(callback);
        }

        @Override
        protected void execute() throws Exception {
            task.execute(this);
        }

        @Override
        public void done(T result) {
            lifecycle().done(result);
        }

        @Override
        public void failed(Throwable e) {
            lifecycle().failed(e);
        }

        @Override
        public void cancelled() {
            lifecycle().cancelled();
        }

        private void checkInterrupted() throws InterruptedException {
            if (Thread.interrupted() || isExceptional() || shutdown) {
                throw new InterruptedException();
            }
        }

        @SuppressWarnings("unchecked")
        private <R> R wrapExceptionHandler(Class<?> clazz, R obj) {
            InvocationHandler handler = new ExceptionCatcher(obj);
            return (R) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{clazz}, handler);
        }

        private <R> Callback<R> wrapCallback(Callback<R> callback) {
            return wrapExceptionHandler(Callback.class, callback);
        }

        private <R> DownloadCallback<R> wrapDownloadCallback(DownloadCallback<R> callback) {
            return wrapExceptionHandler(DownloadCallback.class, callback);
        }

        private <R> CombinedDownloadCallback<R> wrapCombinedDownloadCallback(CombinedDownloadCallback<R> callback) {
            return wrapExceptionHandler(CombinedDownloadCallback.class, callback);
        }

        private class ExceptionCatcher implements InvocationHandler {

            private Object target;

            public ExceptionCatcher(Object target) {
                Objects.requireNonNull(target);
                this.target = target;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(target, args);
                } catch (Throwable e) {
                    Throwable exception = e;
                    if (e instanceof InvocationTargetException) {
                        exception = e.getCause();
                        if (exception == null)
                            exception = e;
                    }
                    lifecycle().failed(exception);
                }
                return null;
            }

        }

        private class SubtaskCounter {

            private final List<Callable<?>> taskWaitNodes = new Vector<>();
            private volatile int count = 0;

            public void countUp() {
                synchronized (this) {
                    count++;
                    if (count < 1)
                        throw new IllegalStateException("Invalid task count: " + count);
                }
            }

            public void countDown() {
                List<Callable<?>> copiedWaitNodes = null;
                synchronized (this) {
                    count--;
                    if (count == 0) {
                        copiedWaitNodes = new Vector<>(taskWaitNodes);
                        taskWaitNodes.clear();
                    } else if (count < 0) {
                        throw new IllegalStateException("Invalid task count: " + count);
                    }
                }

                if (copiedWaitNodes != null)
                    for (Callable<?> waitNode : copiedWaitNodes)
                        doCallback(waitNode);
            }

            public void awaitAllTasks(Callable<Void> callback) {
                synchronized (this) {
                    if (count > 0) {
                        taskWaitNodes.add(callback);
                        return;
                    }
                }
                doCallback(callback);
            }

            private void doCallback(Callable<?> callback) {
                try {
                    callback.call();
                } catch (Throwable e) {
                    lifecycle().failed(e);
                }
            }

        }

        private class SubtaskCountdownAction implements Runnable {

            @Override
            public void run() {
                subtaskCounter.countDown();
            }
        }

        private class FatalSubtaskCallback<R> extends CallbackAdapter<R> {

            @Override
            public void failed(Throwable e) {
                lifecycle().failed(e);
            }

            @Override
            public void cancelled() {
                lifecycle().cancelled();
            }

        }

        private class SubDownloadTaskMapper<R> extends CallbackAdapter<R> {

            @Override
            public <S> DownloadCallback<S> taskStart(DownloadTask<S> subtask) {
                return callback.taskStart(subtask);
            }

        }

    }

    private class TaskInactiver implements Runnable {

        private final Future<?> future;

        public TaskInactiver(Future<?> future) {
            Objects.requireNonNull(future);
            this.future = future;
        }

        @Override
        public void run() {
            tasks.remove(future);
        }

    }

}
