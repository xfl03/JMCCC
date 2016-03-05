package org.to2mbn.jmccc.mcdownloader.download.concurrent;

abstract public class AbstractAsyncCallback<T> implements Callback<T> {

	@Override
	public void done(T result) {
	}

	@Override
	public void failed(Throwable e) {
	}

	@Override
	public void cancelled() {
	}

}
