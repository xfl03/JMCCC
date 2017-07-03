package org.to2mbn.jmccc.mcdownloader;

import java.util.Objects;
import java.util.function.Supplier;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;

public class MinecraftDownloaderBuilder implements Supplier<MinecraftDownloader> {

	public static MinecraftDownloaderBuilder create(Supplier<? extends Downloader> underlying) {
		return new MinecraftDownloaderBuilder(underlying);
	}

	public static MinecraftDownloaderBuilder create() {
		return create(CombinedDownloaderBuilder.create());
	}

	public static MinecraftDownloader buildDefault(Supplier<? extends Downloader> underlying) {
		return new MinecraftDownloaderBuilder(underlying).get();
	}

	public static MinecraftDownloader buildDefault() {
		return buildDefault(CombinedDownloaderBuilder.create());
	}

	protected final Supplier<? extends Downloader> underlying;
	protected Supplier<MinecraftDownloadProvider> providerChain;

	protected MinecraftDownloaderBuilder(Supplier<? extends Downloader> underlying) {
		this.underlying = underlying;
	}

	public MinecraftDownloaderBuilder providerChain(Supplier<MinecraftDownloadProvider> providerChain) {
		this.providerChain = providerChain;
		return this;
	}

	@Override
	public MinecraftDownloader get() {
		MinecraftDownloadProvider provider = providerChain == null
				? DownloadProviderChain.buildDefault()
				: Objects.requireNonNull(providerChain.get(), "providerChain builder returns null");

		CombinedDownloader combinedDownloader = null;
		Downloader underlying = null;
		try {
			underlying = Objects.requireNonNull(this.underlying.get(), "Underlying downloader builder returns null");

			if (underlying instanceof CombinedDownloader) {
				combinedDownloader = (CombinedDownloader) underlying;
			} else {
				Downloader f_underlying = underlying;
				combinedDownloader = CombinedDownloaderBuilder.buildDefault(() -> f_underlying);
			}

			return new MinecraftDownloaderImpl(combinedDownloader, provider);
		} catch (Throwable e) {
			if (combinedDownloader != null) {
				try {
					combinedDownloader.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			if (underlying != null) {
				try {
					underlying.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			throw e;
		}
	}

}
