package org.to2mbn.jmccc.mcdownloader;

import java.util.Objects;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChainBuilder;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.util.Builder;

public class MinecraftDownloaderBuilder implements Builder<MinecraftDownloader> {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	public static MinecraftDownloader buildDefault() {
		return new MinecraftDownloaderBuilder().build();
	}

	protected boolean checkLibrariesHash = true;
	protected boolean checkAssetsHash = true;
	protected boolean updateSnapshots = true;
	protected Builder<MinecraftDownloadProvider> providerChain;
	protected Builder<CombinedDownloader> combinedDownloader;
	protected Builder<Downloader> downloader;

	public MinecraftDownloaderBuilder checkLibrariesHash(boolean checkLibrariesHash) {
		this.checkLibrariesHash = checkLibrariesHash;
		return this;
	}

	public MinecraftDownloaderBuilder checkAssetsHash(boolean checkAssetsHash) {
		this.checkAssetsHash = checkAssetsHash;
		return this;
	}

	public MinecraftDownloaderBuilder updateSnapshots(boolean updateSnapshots) {
		this.updateSnapshots = updateSnapshots;
		return this;
	}

	public MinecraftDownloaderBuilder providerChain(Builder<MinecraftDownloadProvider> providerChain) {
		this.providerChain = providerChain;
		return this;
	}

	public MinecraftDownloaderBuilder combinedDownloader(Builder<CombinedDownloader> combinedDownloader) {
		this.combinedDownloader = combinedDownloader;
		return this;
	}

	public MinecraftDownloaderBuilder downloader(Builder<Downloader> downloader) {
		this.downloader = downloader;
		return this;
	}

	@Override
	public MinecraftDownloader build() {
		MinecraftDownloadProvider provider = providerChain == null
				? DownloadProviderChainBuilder.buildDefault()
				: Objects.requireNonNull(providerChain.build(), "providerChain builder retuns null");

		CombinedDownloader combinedDownloader = null;
		try {
			combinedDownloader = this.combinedDownloader == null
					? CombinedDownloaderBuilder.create().downloader(this.downloader).build()
					: Objects.requireNonNull(this.combinedDownloader.build(), "combinedDownloader builder retuns null");

			return new MinecraftDownloaderImpl(combinedDownloader, provider, checkLibrariesHash, checkAssetsHash, updateSnapshots);
		} catch (Throwable e) {
			if (combinedDownloader != null) {
				try {
					combinedDownloader.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
		}

		return null;
	}

}
