package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;

class CombinedDownloadCallbackFromCallback<T> extends AbstractCombinedDownloadCallback<T> {

	private Callback<T> proxied;

	public CombinedDownloadCallbackFromCallback(Callback<T> proxied) {
		this.proxied = proxied;
	}

	@Override
	public void done(T result) {
		proxied.done(result);
	}

	@Override
	public void failed(Throwable e) {
		proxied.failed(e);
	}

	@Override
	public void cancelled() {
		proxied.cancelled();
	}

}
