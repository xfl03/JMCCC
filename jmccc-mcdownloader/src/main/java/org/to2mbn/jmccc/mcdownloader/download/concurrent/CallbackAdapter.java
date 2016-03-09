package org.to2mbn.jmccc.mcdownloader.download.concurrent;

abstract public class CallbackAdapter<V> implements Callback<V> {

	@Override
	public void done(V result) {
	}

	@Override
	public void failed(Throwable e) {
	}

	@Override
	public void cancelled() {
	}

}
