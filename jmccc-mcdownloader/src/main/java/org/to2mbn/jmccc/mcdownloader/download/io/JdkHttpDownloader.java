package org.to2mbn.jmccc.mcdownloader.download.io;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.*;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadSession;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.util.ThreadPoolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

class JdkHttpDownloader implements Downloader {

    private static final int BUFFER_SIZE = 8192;

    private static final Logger LOGGER = Logger.getLogger(JdkHttpDownloader.class.getCanonicalName());
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Set<Future<?>> tasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());
    private ExecutorService executor;

    private int connectTimeout;
    private int readTimeout;
    private Proxy proxy;

    private volatile boolean shutdown;
    public JdkHttpDownloader(int maxConns, int connectTimeout, int readTimeout, long poolThreadLivingTime, TimeUnit poolThreadLivingTimeUnit, Proxy proxy) {
        Objects.requireNonNull(proxy);

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.proxy = proxy;
        executor = ThreadPoolUtils.createPool(maxConns, poolThreadLivingTime, poolThreadLivingTimeUnit, "jdkDownloader.io");
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
        return download(task, callback, 1);
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> downloadTask, DownloadCallback<T> callback, int tries) {
        /*
         * # Submit task
         * ++++ read lock
         * 	1. Has the shutdown flag been set? ................................................... read shutdown
         * 		Yes - Reject execution.
         * 		No - Go on.
         *
         * 	2. Create a task handler, store it in tasks. ......................................... write tasks
         *
         * 	3. Start the task handler. ........................................................... read executor
         * ---- read unlock
         */

        Objects.requireNonNull(downloadTask);
        if (tries < 1)
            throw new IllegalArgumentException("tries < 1");

        CallbackFutureTask<T> task = new CallbackFutureTask<>(new CallableDownloadTask<>(
                downloadTask,
                callback == null ? DownloadCallbacks.<T>empty() : callback,
                tries));

        Callback<T> statusCallback = Callbacks.whatever(new TaskInactiver(task));
        if (callback != null) {
            statusCallback = Callbacks.group(statusCallback, callback);
        }
        task.setCallback(callback);

        Lock lock = rwlock.readLock();
        lock.lock();
        try {
            if (shutdown)
                throw new RejectedExecutionException("The downloader has been shutdown.");

            tasks.add(task);
            executor.execute(task);
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
         * 	1. Has the shutdown flag been set? ................................................... read shutdown
         * 		Yes - Do nothing.
         * 		No - Go on.
         *
         * 	2. Set the shutdown flag. ............................................................ write shutdown
         *
         * 	3. Cancel all the tasks. ............................................................. read tasks
         *
         * 	4. Shutdown the executor. ............................................................ write executor
         * ---- write unlock
         */

        Lock lock = rwlock.writeLock();
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
        executor = null;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    private class CallableDownloadTask<T> implements Callable<T> {

        private final DownloadTask<T> task;
        private final DownloadCallback<T> callback;
        private final int maxTries;

        private boolean skipRetry = false;

        public CallableDownloadTask(DownloadTask<T> task, DownloadCallback<T> callback, int maxTries) {
            Objects.requireNonNull(task);
            Objects.requireNonNull(callback);
            if (maxTries < 1)
                throw new IllegalArgumentException(String.valueOf(maxTries));

            this.task = task;
            this.callback = callback;
            this.maxTries = maxTries;
        }

        @Override
        public T call() throws Exception {
            int currentTries = 0;
            for (; ; ) {
                try {
                    return download();
                } catch (IOException e) {
                    checkInterrupted();
                    currentTries++;
                    if (currentTries < maxTries && !skipRetry && DownloaderHelper.shouldRetry(e)) {
                        callback.retry(e, currentTries, maxTries);
                    } else {
                        throw e;
                    }
                }
            }
        }

        private T download() throws Exception {
            return downloadCore(task.getURI().toURL());
        }

        private T downloadCore(URL url) throws Exception {
            URLConnection connection = url.openConnection(proxy);
            connection.setReadTimeout(readTimeout);
            connection.setConnectTimeout(connectTimeout);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Accept-Encoding", "gzip");
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod("GET");
                ((HttpURLConnection) connection).setInstanceFollowRedirects(true);
            }
            connection.connect();

            try {
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection urlConnection = (HttpURLConnection) connection;
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 301 || responseCode == 302 || responseCode == 307) {
                        return downloadCore(new URL(urlConnection.getHeaderField("Location")));
                    }
                    if (responseCode < 200 || responseCode > 299) {
                        // non-2xx response code
                        throw new IllegalHttpResponseCodeException(urlConnection.getHeaderField(0), responseCode);
                    }
                }

                String contentLengthStr = connection.getHeaderField("Content-Length");
                long contentLength = -1;
                if (contentLengthStr != null) {
                    try {
                        contentLength = Long.parseLong(contentLengthStr);
                        if (contentLength < 0) {
                            LOGGER.warning("Invalid Content-Length: " + contentLengthStr + ", ignoring");
                            contentLength = -1;
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid Content-Length: " + contentLengthStr + ", ignoring: " + e);
                    }
                }

                checkInterrupted();

                DownloadSession<T> session = (contentLength == -1)
                        ? task.createSession()
                        : task.createSession(contentLength);

                if (connection instanceof HttpURLConnection && "gzip".equals(connection.getHeaderField("Content-Encoding"))) {
                    session = new GzipDownloadSession<>(session);
                }

                long downloaded = 0;

                try (InputStream in = connection.getInputStream()) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = in.read(buf)) != -1) {
                        checkInterrupted();
                        downloaded += read;
                        session.receiveData(ByteBuffer.wrap(buf, 0, read));
                        skipRetry = true;
                        callback.updateProgress(downloaded, contentLength);
                        skipRetry = false;
                    }
                } catch (Throwable e) {
                    session.failed();
                    throw e;
                }
                return session.completed();
            } finally {
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).disconnect();
                }
            }
        }

        private void checkInterrupted() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
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
