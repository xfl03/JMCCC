package org.to2mbn.jmccc.util;

public final class Builders {

    private Builders() {
    }

    public static <T> Builder<T> of(final T o) {
        return new Builder<T>() {

            @Override
            public T build() {
                return o;
            }
        };
    }

}
