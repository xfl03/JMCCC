package org.to2mbn.jmccc.mcdownloader.download.cache;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.cache.provider.CacheProvider;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CompletedFuture;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadSession;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

class CachedDownloader implements Downloader {

    private static final Logger LOGGER = Logger.getLogger(CachedDownloader.class.getCanonicalName());
    private Downloader upstream;
    private CacheProvider<URI, byte[]> cacheProvider;
    public CachedDownloader(Downloader upstream, CacheProvider<URI, byte[]> cacheProvider) {
        this.upstream = Objects.requireNonNull(upstream);
        this.cacheProvider = Objects.requireNonNull(cacheProvider);
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
        return downloadIfNecessary(task, callback, -1);
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
        return downloadIfNecessary(task, callback, tries);
    }

    @Override
    public void shutdown() {
        try {
            upstream.shutdown();
        } finally {
            try {
                cacheProvider.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Couldn't close cache provider: " + cacheProvider, e);
            }
        }
    }

    @Override
    public boolean isShutdown() {
        return upstream.isShutdown();
    }

    private <T> Future<T> downloadIfNecessary(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
        if (task.isCacheable()) {
            URI uri = task.getURI();
            String pool = resolveCachePool(task.getCachePool());

            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.finer(String.format("Resolved the cache pool of [%s]: [%s] -> [%s]", uri, task.getCachePool(), pool));

            byte[] cached = cacheProvider.get(pool, uri);
            if (cached == null) {
                return submitToUpstream(new CachingDownloadTask<>(task), callback, tries);
            } else {

                T result;
                try {
                    result = processCache(task, cached);
                } catch (Throwable e) {
                    cacheProvider.remove(pool, uri);

                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.log(Level.FINE, String.format("Removed cache [%s] from [%s] because an exception has thrown when applying cache", uri, pool), e);

                    return submitToUpstream(new CachingDownloadTask<>(task), callback, tries);
                }

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(String.format("Applied cache [%s] from [%s], length=%d", uri, pool, cached.length));

                if (callback != null) {
                    callback.done(result);
                }
                return new CompletedFuture<T>(result);
            }
        } else {
            return submitToUpstream(task, callback, tries);
        }
    }

    private <T> Future<T> submitToUpstream(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
        if (tries == -1) {
            return upstream.download(task, callback);
        } else {
            return upstream.download(task, callback, tries);
        }
    }

    private <T> T processCache(DownloadTask<T> task, byte[] cached) throws Exception {
        DownloadSession<T> session = task.createSession(cached.length);
        try {
            session.receiveData(ByteBuffer.wrap(cached));
        } catch (Throwable e) {
            session.failed();
            throw e;
        }
        return session.completed();
    }

    private String resolveCachePool(String unresolved) {
        if (unresolved == null) {
            return CacheNames.DEFAULT;
        }
        for (; ; ) {
            if (cacheProvider.hasCache(unresolved)) {
                return unresolved;
            }
            int lastDot = unresolved.lastIndexOf('.');
            if (lastDot == -1) {
                return CacheNames.DEFAULT;
            }
            unresolved = unresolved.substring(0, lastDot);
        }
    }

    @Override
    public String toString() {
        return String.format("CachedDownloader [upstream=%s, cacheProvider=%s]", upstream, cacheProvider);
    }

    private class CachingDownloadTask<T> extends DownloadTask<T> {

        private final DownloadTask<T> proxiedTask;

        public CachingDownloadTask(DownloadTask<T> proxiedTask) {
            super(proxiedTask.getURI());
            this.proxiedTask = proxiedTask;
        }

        @Override
        public DownloadSession<T> createSession() throws IOException {
            return new CachingDownloadSession(proxiedTask.createSession(), 8192);
        }

        @Override
        public DownloadSession<T> createSession(long length) throws IOException {
            return new CachingDownloadSession(proxiedTask.createSession(length), length);
        }

        private class CachingDownloadSession implements DownloadSession<T> {

            private final DownloadSession<T> proxiedSession;

            // use SoftReference to prevent OOM
            private SoftReference<ByteArrayOutputStream> bufRef;

            public CachingDownloadSession(DownloadSession<T> proxiedSession, long length) {
                this.proxiedSession = proxiedSession;
                if (length < Integer.MAX_VALUE) {
                    try {
                        bufRef = new SoftReference<>(new ByteArrayOutputStream((int) length));
                    } catch (OutOfMemoryError e) {
                        dropCache();
                    }
                }
            }

            @Override
            public void receiveData(ByteBuffer data) throws IOException {
                byte[] copiedData = new byte[data.remaining()];
                data.get(copiedData);

                proxiedSession.receiveData(ByteBuffer.wrap(copiedData));

                if (bufRef != null) {
                    try {
                        ByteArrayOutputStream buf = bufRef.get();
                        if (buf != null) {
                            buf.write(copiedData);
                        }
                    } catch (OutOfMemoryError e) {
                        dropCache();
                    }
                }
            }

            @Override
            public T completed() throws Exception {
                T result;
                try {
                    result = proxiedSession.completed();
                } catch (Throwable e) {
                    dropCache();
                    throw e;
                }
                saveCache();
                return result;
            }

            @Override
            public void failed() throws Exception {
                dropCache();
                proxiedSession.failed();
            }

            private void dropCache() {
                if (bufRef != null) {
                    bufRef.clear();
                    bufRef = null;
                }
            }

            private void saveCache() {
                if (bufRef != null) {
                    try {
                        ByteArrayOutputStream buf = bufRef.get();
                        if (buf != null) {
                            byte[] data = buf.toByteArray();
                            URI uri = proxiedTask.getURI();
                            String pool = resolveCachePool(proxiedTask.getCachePool());
                            cacheProvider.put(pool, uri, data);

                            if (LOGGER.isLoggable(Level.FINE))
                                LOGGER.fine(String.format("Cached [%s] into [%s], length=%d", uri, pool, data.length));
                        }
                    } catch (OutOfMemoryError e) {
                        dropCache();
                    }
                }
            }

        }

    }

}
