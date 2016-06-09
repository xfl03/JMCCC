package org.to2mbn.jmccc.util;

public final class Builders {

	public static <T> Builder<T> of(final T o) {
		return new Builder<T>() {

			@Override
			public T build() {
				return o;
			}
		};
	}

	private Builders() {}

}
