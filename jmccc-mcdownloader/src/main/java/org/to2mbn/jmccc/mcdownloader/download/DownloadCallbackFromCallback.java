package org.to2mbn.jmccc.mcdownloader.download;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;

class DownloadCallbackFromCallback<T> extends AbstractDownloadCallback<T> {

	private Callback<T> proxied;

	public DownloadCallbackFromCallback(Callback<T> proxied) {
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
