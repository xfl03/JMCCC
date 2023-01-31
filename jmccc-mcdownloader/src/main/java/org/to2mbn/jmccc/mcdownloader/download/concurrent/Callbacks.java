package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Collection;
import java.util.Objects;

public final class Callbacks {

    private Callbacks() {
    }

    @SafeVarargs
    public static <T> Callback<T> group(Callback<T>... callbacks) {
        Objects.requireNonNull(callbacks);
        return new CallbackGroup<>(callbacks);
    }

    public static <T> Callback<T> group(Collection<Callback<T>> callbacks) {
        Objects.requireNonNull(callbacks);
        @SuppressWarnings("unchecked")
        Callback<T>[] result = callbacks.toArray(new Callback[callbacks.size()]);
        return new CallbackGroup<>(result);
    }

    public static <T> Callback<T> whatever(Runnable callback) {
        Objects.requireNonNull(callback);
        return new WhateverCallback<>(callback);
    }

    public static <T> Callback<T> empty() {
        return new EmptyCallback<>();
    }
}
