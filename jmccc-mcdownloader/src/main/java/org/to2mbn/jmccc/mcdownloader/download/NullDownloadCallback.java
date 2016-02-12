package org.to2mbn.jmccc.mcdownloader.download;

class NullDownloadCallback<T> implements DownloadCallback<T> {

	@Override
	public void done(T result) {
	}

	@Override
	public void failed(Throwable e) {
	}

	@Override
	public void cancelled() {
	}

	@Override
	public void updateProgress(long done, long total) {
	}

	@Override
	public void retry(Throwable e, int current, int max) {
	}

}