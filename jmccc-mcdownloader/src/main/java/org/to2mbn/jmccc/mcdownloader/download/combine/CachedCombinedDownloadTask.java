package org.to2mbn.jmccc.mcdownloader.download.combine;

class CachedCombinedDownloadTask<T> extends CombinedDownloadTask<T> {

	private CombinedDownloadTask<T> proxied;
	private CacheStrategy strategy;

	public CachedCombinedDownloadTask(CombinedDownloadTask<T> proxied, CacheStrategy strategy) {
		this.proxied = proxied;
		this.strategy = strategy;
	}

	@Override
	public void execute(CombinedDownloadContext<T> context) throws Exception {
		proxied.execute(new CachedCombinedDownloadContext<>(context, strategy));
	}

	@Override
	public CacheStrategy getCacheStrategy() {
		return strategy;
	}

}
