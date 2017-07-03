package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderBuilders;
import org.to2mbn.jmccc.mcdownloader.util.ThreadPoolUtils;

public class CombinedDownloaderBuilder implements Supplier<CombinedDownloader> {

	public static CombinedDownloaderBuilder create(Supplier<Downloader> underlying) {
		return new CombinedDownloaderBuilder(underlying);
	}

	public static CombinedDownloaderBuilder create() {
		return create(DownloaderBuilders.cacheableDownloader());
	}

	public static CombinedDownloader buildDefault(Supplier<Downloader> underlying) {
		return create(underlying).get();
	}

	public static CombinedDownloader buildDefault() {
		return buildDefault(DownloaderBuilders.cacheableDownloader());
	}

	protected final Supplier<Downloader> underlying;
	protected int threadPoolSize = Runtime.getRuntime().availableProcessors();
	protected long threadPoolKeepAliveTime = 10;
	protected TimeUnit threadPoolKeepAliveTimeUnit = TimeUnit.SECONDS;
	protected int defaultTries = 3;

	protected CombinedDownloaderBuilder(Supplier<Downloader> underlying) {
		this.underlying = Objects.requireNonNull(underlying);
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
	public CombinedDownloader get() {
		ExecutorService pool = null;
		Downloader downloader = null;
		try {
			pool = ThreadPoolUtils.createPool(threadPoolSize, threadPoolKeepAliveTime, threadPoolKeepAliveTimeUnit, "combinedDownloader");
			downloader = Objects.requireNonNull(this.underlying.get(), "Underlying downloader builder returns null");
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
