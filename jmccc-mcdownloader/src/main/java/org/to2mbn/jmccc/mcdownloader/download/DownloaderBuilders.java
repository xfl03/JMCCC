package org.to2mbn.jmccc.mcdownloader.download;

import java.util.function.Supplier;
import org.to2mbn.jmccc.mcdownloader.download.cache.CachedDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.io.JdkDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.io.async.HttpAsyncDownloaderBuilder;

public final class DownloaderBuilders {

	public static Supplier<Downloader> downloader() {
		if (HttpAsyncDownloaderBuilder.isAvailable()) {
			return HttpAsyncDownloaderBuilder.create();
		} else {
			return JdkDownloaderBuilder.create();
		}
	}

	public static Supplier<Downloader> cacheableDownloader(Supplier<Downloader> underlying) {
		if (CachedDownloaderBuilder.isAvailable()){
			return CachedDownloaderBuilder.create(underlying);
		} else {
			return underlying;
		}
	}

	public static Supplier<Downloader> cacheableDownloader() {
		return cacheableDownloader(downloader());
	}

	private DownloaderBuilders() {}
}
