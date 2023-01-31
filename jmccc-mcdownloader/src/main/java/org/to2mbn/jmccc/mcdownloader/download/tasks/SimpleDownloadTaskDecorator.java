package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.net.URI;

/**
 * A subclass of DownloadTaskDecorator that has the same SRC and DEST.
 *
 * @param <T> the result type
 * @author yushijinhun
 */
public class SimpleDownloadTaskDecorator<T> extends DownloadTaskDecorator<T, T> {

    public SimpleDownloadTaskDecorator(DownloadTask<T> delegated) {
        super(delegated);
    }

    public SimpleDownloadTaskDecorator(URI uri, DownloadTask<T> delegated) {
        super(uri, delegated);
    }

    @Override
    protected DownloadSession<T> createSessionDelegate(DownloadSession<T> toDelegate) {
        return toDelegate;
    }

}
