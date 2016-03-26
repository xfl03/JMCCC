package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;

class CachedDownloadTask<T> extends DownloadTask<T> {

	private DownloadTask<T> proxied;
	private boolean cachable;

	public CachedDownloadTask(DownloadTask<T> proxied, boolean cachable) {
		super(proxied.getURI());
		this.proxied = proxied;
		this.cachable = cachable;
	}

	@Override
	public boolean isCacheable() {
		return cachable;
	}

	@Override
	public DownloadSession<T> createSession() throws IOException {
		return proxied.createSession();
	}

	@Override
	public DownloadSession<T> createSession(long length) throws IOException {
		return proxied.createSession(length);
	}

}
