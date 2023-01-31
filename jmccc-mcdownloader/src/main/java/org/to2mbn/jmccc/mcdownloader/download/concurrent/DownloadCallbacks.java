package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Collection;
import java.util.Objects;

public final class DownloadCallbacks {

    private DownloadCallbacks() {
    }

    @SafeVarargs
    public static <T> DownloadCallback<T> group(DownloadCallback<T>... callbacks) {
        Objects.requireNonNull(callbacks);
        return new DownloadCallbackGroup<>(callbacks);
    }

    public static <T> DownloadCallback<T> group(Collection<DownloadCallback<T>> callbacks) {
        Objects.requireNonNull(callbacks);
        @SuppressWarnings("unchecked")
        DownloadCallback<T>[] result = callbacks.toArray(new DownloadCallback[callbacks.size()]);
        return new DownloadCallbackGroup<>(result);
    }

    public static <T> DownloadCallback<T> fromCallback(Callback<T> callback) {
        return new AdaptedCallback<>(callback);
    }

    public static <T> DownloadCallback<T> whatever(Runnable callback) {
        Callback<T> c = Callbacks.whatever(callback);
        return fromCallback(c);
    }

    public static <T> DownloadCallback<T> empty() {
        return new EmptyCallback<>();
    }
}
