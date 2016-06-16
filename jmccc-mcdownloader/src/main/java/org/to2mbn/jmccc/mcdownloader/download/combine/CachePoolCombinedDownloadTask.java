package org.to2mbn.jmccc.mcdownloader.download.combine;

class CachePoolCombinedDownloadTask<T> extends CombinedDownloadTask<T> {

	private CombinedDownloadTask<T> proxied;
	private String cachePool;

	public CachePoolCombinedDownloadTask(CombinedDownloadTask<T> proxied, String cachePool) {
		this.proxied = proxied;
		this.cachePool = cachePool;
	}

	@Override
	public void execute(CombinedDownloadContext<T> context) throws Exception {
		proxied.execute(new CachePoolCombinedDownloadContext<>(context, cachePool));
	}

	@Override
	public CacheStrategy getCacheStrategy() {
		return proxied.getCacheStrategy();
	}

	@Override
	public String getCachePool() {
		return cachePool;
	}

}
