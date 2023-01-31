package org.to2mbn.jmccc.mcdownloader.download.io.async;

import org.apache.http.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.*;
import org.to2mbn.jmccc.mcdownloader.download.io.DownloaderHelper;
import org.to2mbn.jmccc.mcdownloader.download.io.GzipDownloadSession;
import org.to2mbn.jmccc.mcdownloader.download.io.IllegalHttpResponseCodeException;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadSession;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class HttpAsyncDownloader implements Downloader {

    private static final Logger LOGGER = Logger.getLogger(HttpAsyncDownloader.class.getCanonicalName());

    private static final int RUNNING = 0;
    private static final int SHUTTING_DOWN = 1;
    private static final int TERMINATED = 2;
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Set<Future<?>> tasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());
    private CloseableHttpAsyncClient httpClient;
    private ExecutorService bootstrapPool;
    private volatile int status = RUNNING;

    public HttpAsyncDownloader(CloseableHttpAsyncClient client, ExecutorService bootstrapPool) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(bootstrapPool);
        this.httpClient = client;
        this.bootstrapPool = bootstrapPool;

        httpClient.start();
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> downloadTask, DownloadCallback<T> callback, int tries) {
        /*
         * # Submit task
         * ++++ read lock
         * 	1. Has the shutdown flag been set? ................................................... read status
         * 		Yes - Reject execution.
         * 		No - Go on.
         *
         * 	2. Create a task handler, store it in tasks. ......................................... write tasks
         *
         * 	3. Start the task handler. ........................................................... read status
         * ---- read unlock
         */

        Objects.requireNonNull(downloadTask);
        if (tries < 1)
            throw new IllegalArgumentException("tries < 1");

        CallbackAsyncTask<T> task = new AsyncDownloadTask<T>(downloadTask, callback == null ? DownloadCallbacks.<T>empty() : callback, tries);
        Callback<T> statusCallback = Callbacks.whatever(new TaskInactiver(task));
        if (callback != null)
            statusCallback = Callbacks.group(statusCallback, callback);
        task.setCallback(statusCallback);

        Lock lock = rwlock.readLock();
        lock.lock();
        try {
            if (isShutdown())
                throw new RejectedExecutionException("The downloader has been shutdown.");

            bootstrapPool.execute(task);

            tasks.add(task);
        } finally {
            lock.unlock();
        }

        return task;
    }

    @Override
    public void shutdown() {
        /*
         * # Shutdown
         * ++++ write lock
         * 	1. Is the downloader running? ........................................................ read status
         * 		Yes - Go on.
         * 		No - Do nothing.
         *
         * 	2. Set the status to SHUTTING_DOWN. .................................................... write status
         *
         * 	3. Is any task running? .............................................................. read tasks
         * 		Yes -
         * 				---- write unlock
         * 				1. Cancel all the tasks and then do nothing.
         * 					Let the last terminated thread cleanup.
         * 		No -
         * 				1. Set the status to TERMINATED. ........................................ write status
         * 				---- write unlock
         * 				2. Cleanup. .............................................................. write status
         *
         */
        boolean isTasksEmpty;

        Lock lock = rwlock.writeLock();
        lock.lock();
        try {
            if (isShutdown()) {
                return;
            }

            status = SHUTTING_DOWN;
            isTasksEmpty = tasks.isEmpty();
            if (isTasksEmpty) {
                status = TERMINATED;
            }
        } finally {
            lock.unlock();
        }

        bootstrapPool.shutdownNow();

        if (isTasksEmpty) {
            completeShutdown();
        } else {
            for (Future<?> task : tasks)
                task.cancel(true);
        }
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
        return download(task, callback, 1);
    }

    @Override
    public boolean isShutdown() {
        return status != RUNNING;
    }

    private void completeShutdown() {
        bootstrapPool = null;
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Couldn't shutdown http async client", e);
        }
        httpClient = null;
    }

    private static class DownloadSessionHandler<T> {

        public final HttpAsyncResponseConsumer<T> consumer;
        public final FutureCallback<T> callback;
        private final DownloadTask<T> task;
        private final DownloadCallback<T> downloadCallback;

        private volatile DownloadSession<T> session;
        private volatile Throwable resultBuildingEx;

        public DownloadSessionHandler(DownloadTask<T> task, DownloadCallback<T> downloadCallback) {
            Objects.requireNonNull(task);
            Objects.requireNonNull(downloadCallback);
            this.task = task;
            this.downloadCallback = downloadCallback;

            consumer = new DataConsumer();
            callback = new DownloadCallbackAdapter(downloadCallback);
        }

        private class DataConsumer extends AsyncByteConsumer<T> {

            private volatile long contextLength = -1;
            private volatile long received = 0;

            @Override
            protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException {
                if (session == null)
                    session = task.createSession();

                received += buf.remaining();
                session.receiveData(buf);
                downloadCallback.updateProgress(received, contextLength);
            }

            @Override
            protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine != null) {
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode < 200 || statusCode > 299)
                        // non-2xx response code
                        throw new IllegalHttpResponseCodeException(statusLine.toString(), statusCode);
                }

                if (session == null) {
                    boolean gzipOn = false;
                    HttpEntity httpEntity = response.getEntity();
                    if (httpEntity != null) {
                        long contextLength = httpEntity.getContentLength();
                        if (contextLength >= 0) {
                            this.contextLength = contextLength;
                        }

                        Header contentEncodingHeader = httpEntity.getContentEncoding();
                        if (contentEncodingHeader != null && "gzip".equals(contentEncodingHeader.getValue())) {
                            gzipOn = true;
                        }
                    }

                    session = contextLength > 0
                            ? task.createSession(contextLength)
                            : task.createSession();

                    if (gzipOn) {
                        session = new GzipDownloadSession<>(session);
                    }
                }
            }

            @Override
            protected T buildResult(HttpContext context) throws Exception {
                T result = null;
                try {
                    if (session == null) {
                        throw new IllegalStateException("Download session is not active");
                    }

                    result = session.completed();
                    resultBuildingEx = null;
                } catch (Throwable e) {
                    resultBuildingEx = e;
                }
                return result;
            }

        }

        private class DownloadCallbackAdapter implements FutureCallback<T> {

            private final Callback<T> adapted;

            public DownloadCallbackAdapter(Callback<T> adapted) {
                this.adapted = adapted;
            }

            @Override
            public void completed(T result) {
                if (resultBuildingEx == null) {
                    adapted.done(result);
                } else {
                    adapted.failed(resultBuildingEx);
                }
            }

            @Override
            public void failed(Exception ex) {
                if (session != null) {
                    try {
                        session.failed();
                    } catch (Throwable e) {
                        if (e != ex)
                            ex.addSuppressed(e);
                    }
                }
                adapted.failed(ex);
            }

            @Override
            public void cancelled() {
                if (session != null) {
                    try {
                        session.failed();
                    } catch (Throwable e) {
                        adapted.failed(e);
                        return;
                    }
                }
                adapted.cancelled();
            }

        }

    }

    private class AsyncDownloadTask<T> extends CallbackAsyncTask<T> {

        private final DownloadTask<T> task;
        private final DownloadCallback<T> callback;
        private final int maxTries;
        private volatile int currentTries;

        public AsyncDownloadTask(DownloadTask<T> task, DownloadCallback<T> callback, int maxTries) {
            Objects.requireNonNull(task);
            Objects.requireNonNull(callback);
            if (maxTries < 1)
                throw new IllegalArgumentException(String.valueOf(maxTries));

            this.task = task;
            this.callback = callback;
            this.maxTries = maxTries;
        }

        @Override
        protected void execute() throws Exception {
            download();
        }

        private void download() {
            if (Thread.interrupted() || isExceptional()) {
                lifecycle().cancelled();
                return;
            }

            FutureManager<T> manager = createFutureManager();
            DownloadRetryHandler retryHandler = new DownloadRetryHandler();
            DownloadSessionHandler<T> handler = new DownloadSessionHandler<>(task, DownloadCallbacks.group(DownloadCallbacks.fromCallback(manager), retryHandler));
            Future<T> downloadFuture = httpClient.execute(HttpAsyncMethods.createGet(task.getURI()), handler.consumer, handler.callback);
            manager.setFuture(downloadFuture);
        }

        private class DownloadRetryHandler implements DownloadCallback<T> {

            private volatile boolean skipRetry;

            @Override
            public void done(T result) {
                skipRetry = true;
                lifecycle().done(result);
                skipRetry = false;
            }

            @Override
            public void failed(Throwable e) {
                currentTries++;
                if (currentTries < maxTries && !skipRetry && DownloaderHelper.shouldRetry(e)) {
                    skipRetry = true;
                    callback.retry(e, currentTries, maxTries);
                    skipRetry = false;
                    download();
                } else {
                    skipRetry = true;
                    lifecycle().failed(e);
                    skipRetry = false;
                }
            }

            @Override
            public void cancelled() {
                skipRetry = true;
                lifecycle().cancelled();
                skipRetry = false;
            }

            @Override
            public void updateProgress(long done, long total) {
                skipRetry = true;
                callback.updateProgress(done, total);
                skipRetry = false;
            }

            @Override
            public void retry(Throwable e, int current, int max) {
                throw new AssertionError("This method shouldn't be invoked.");
            }

        }

    }

    private class TaskInactiver implements Runnable {

        private final Future<?> task;

        public TaskInactiver(Future<?> task) {
            this.task = task;
        }

        @Override
        public void run() {
            /*
             * ## When the task terminates
             * ++++ read lock
             * 	1. Remove itself from tasks. ..................................................... write tasks
             * ---- read unlock
             * 	2. Is status SHUTTING_DOWN? ........................................................ read status
             * 		Yes -
             * 			++++ write lock
             * 			1. Is status SHUTTING_DOWN? ................................................ read status
             * 				Yes - Go on.
             * 				No - Do nothing.
             *
             * 			2. Is tasks empty? ....................................................... read tasks
             * 				Yes - Go on.
             * 				No - Do nothing.
             *
             * 			3. Set status to TERMINATED. ............................................. write status
             * 			---- write unlock
             *
             * 			4. Cleanup. .............................................................. write status
             * 		No - Do nothing.
             */

            Lock rlock = rwlock.readLock();
            rlock.lock();
            try {
                tasks.remove(task);
            } finally {
                rlock.unlock();
            }

            if (status == SHUTTING_DOWN) {
                boolean doCleanup = false;
                Lock wlock = rwlock.writeLock();
                wlock.lock();
                try {
                    if (status == SHUTTING_DOWN && tasks.isEmpty()) {
                        status = TERMINATED;
                        doCleanup = true;
                    }
                } finally {
                    wlock.unlock();
                }
                if (doCleanup) {
                    completeShutdown();
                }
            }
        }

    }

}
