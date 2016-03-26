package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;

class AppendedDownloadTask<R, S> extends DownloadTask<S> {

	private ResultProcessor<R, S> processor;
	private DownloadTask<R> proxied;

	public AppendedDownloadTask(ResultProcessor<R, S> processor, DownloadTask<R> proxied) {
		super(proxied.getURI());
		this.processor = processor;
		this.proxied = proxied;
	}

	@Override
	public DownloadSession<S> createSession() throws IOException {
		return new AppendedDownloadSession<>(processor, proxied.createSession());
	}

	@Override
	public DownloadSession<S> createSession(long length) throws IOException {
		return new AppendedDownloadSession<>(processor, proxied.createSession(length));
	}

	@Override
	public boolean isCacheable() {
		return proxied.isCacheable();
	}

}