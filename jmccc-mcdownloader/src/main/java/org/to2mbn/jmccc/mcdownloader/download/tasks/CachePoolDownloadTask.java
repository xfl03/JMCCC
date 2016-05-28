package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.io.IOException;

class CachePoolDownloadTask<T> extends DownloadTask<T> {

	private DownloadTask<T> proxied;
	private String pool;

	public CachePoolDownloadTask(DownloadTask<T> proxied, String pool) {
		super(proxied.getURI());
		this.proxied = proxied;
		this.pool = pool;
	}

	@Override
	public String getCachePool() {
		return pool;
	}

	@Override
	public boolean isCacheable() {
		return proxied.isCacheable();
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
