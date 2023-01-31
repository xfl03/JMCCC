package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * The superclass of DownloadTask decorators.
 * <p>
 * We use the <b>decorator pattern</b> to "modify" the properties of a
 * DownloadTask, such as {@code cacheable}, {@code cachePool}.
 *
 * @param <SRC>  result type of delegated task
 * @param <DEST> result type of the decorator
 * @author yushijinhun
 * @see SimpleDownloadTaskDecorator
 */
abstract public class DownloadTaskDecorator<SRC, DEST> extends DownloadTask<DEST> {

    protected final DownloadTask<SRC> delegated;

    public DownloadTaskDecorator(DownloadTask<SRC> delegated) {
        this(delegated.getURI(), delegated);
    }

    public DownloadTaskDecorator(URI uri, DownloadTask<SRC> delegated) {
        super(uri);
        this.delegated = Objects.requireNonNull(delegated);
    }

    @Override
    public boolean isCacheable() {
        return delegated.isCacheable();
    }

    @Override
    public String getCachePool() {
        return delegated.getCachePool();
    }

    @Override
    public DownloadSession<DEST> createSession() throws IOException {
        return createSessionDelegate(delegated.createSession());
    }

    @Override
    public DownloadSession<DEST> createSession(long length) throws IOException {
        return createSessionDelegate(delegated.createSession(length));
    }

    abstract protected DownloadSession<DEST> createSessionDelegate(DownloadSession<SRC> toDelegate);

}
