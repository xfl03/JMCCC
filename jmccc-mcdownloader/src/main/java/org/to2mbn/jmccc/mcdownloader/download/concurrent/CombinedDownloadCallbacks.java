package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Collection;
import java.util.Objects;

public final class CombinedDownloadCallbacks {

    private CombinedDownloadCallbacks() {
    }

    @SafeVarargs
    public static <T> CombinedDownloadCallback<T> group(CombinedDownloadCallback<T>... callbacks) {
        Objects.requireNonNull(callbacks);
        return new CombinedDownloadCallbackGroup<>(callbacks);
    }

    public static <T> CombinedDownloadCallback<T> group(Collection<CombinedDownloadCallback<T>> callbacks) {
        Objects.requireNonNull(callbacks);
        @SuppressWarnings("unchecked")
        CombinedDownloadCallback<T>[] result = callbacks.toArray(new CombinedDownloadCallback[callbacks.size()]);
        return new CombinedDownloadCallbackGroup<>(result);
    }

    public static <T> CombinedDownloadCallback<T> fromCallback(Callback<T> callback) {
        return new AdaptedCallback<>(callback);
    }

    public static <T> CombinedDownloadCallback<T> whatever(Runnable callback) {
        Callback<T> c = Callbacks.whatever(callback);
        return fromCallback(c);
    }

    public static <T> CombinedDownloadCallback<T> empty() {
        return new EmptyCallback<>();
    }

}
