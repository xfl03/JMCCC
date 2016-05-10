package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.JdkDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.util.ThreadPoolUtils;
import org.to2mbn.jmccc.util.Builder;

public class CombinedDownloaderBuilder implements Builder<CombinedDownloader> {

	public static CombinedDownloaderBuilder create() {
		return new CombinedDownloaderBuilder();
	}

	public static CombinedDownloader buildDefault() {
		return create().build();
	}

	protected Builder<Downloader> downloader;
	protected int threadPoolSize = Runtime.getRuntime().availableProcessors();
	protected long threadPoolKeepAliveTime = 10;
	protected TimeUnit threadPoolKeepAliveTimeUnit = TimeUnit.SECONDS;
	protected int defaultTries = 3;

	protected CombinedDownloaderBuilder() {}

	public CombinedDownloaderBuilder downloader(Builder<Downloader> downloader) {
		this.downloader = downloader;
		return this;
	}

	public CombinedDownloaderBuilder threadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		return this;
	}

	public CombinedDownloaderBuilder threadPoolKeepAliveTime(long threadPoolKeepAliveTime, TimeUnit unit) {
		this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
		this.threadPoolKeepAliveTimeUnit = unit;
		return this;
	}

	public CombinedDownloaderBuilder defaultTries(int defaultTries) {
		this.defaultTries = defaultTries;
		return this;
	}

	@Override
	public CombinedDownloader build() {
		ExecutorService pool = null;
		Downloader downloader = null;
		try {
			pool = ThreadPoolUtils.createPool(threadPoolSize, threadPoolKeepAliveTime, threadPoolKeepAliveTimeUnit);
			downloader = this.downloader == null
					? (HttpAsyncDownloaderBuilder.isAvailable() ? HttpAsyncDownloaderBuilder.buildDefault() : JdkDownloaderBuilder.buildDefault())
					: Objects.requireNonNull(this.downloader.build(), "downloader builder returns null");
			return new CombinedDownloaderImpl(pool, downloader, defaultTries);
		} catch (Throwable e) {
			if (pool != null) {
				try {
					pool.shutdownNow();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			if (downloader != null) {
				try {
					downloader.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			throw e;
		}
	}

}
