package org.to2mbn.jmccc.mcdownloader;

import java.util.Objects;
import java.util.concurrent.Future;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;

public class MinecraftDownloaderImpl implements MinecraftDownloader {

	private CombinedDownloader combinedDownloader;
	private MinecraftDownloadProvider downloadProvider;
	private boolean checkLibrariesHash;
	private boolean checkAssetsHash;
	private boolean updateSnapshots;

	public MinecraftDownloaderImpl(CombinedDownloader combinedDownloader, MinecraftDownloadProvider downloadProvider, boolean checkLibrariesHash, boolean checkAssetsHash, boolean updateSnapshots) {
		this.combinedDownloader = Objects.requireNonNull(combinedDownloader);
		this.downloadProvider = Objects.requireNonNull(downloadProvider);
		this.checkLibrariesHash = checkLibrariesHash;
		this.checkAssetsHash = checkAssetsHash;
		this.updateSnapshots = updateSnapshots;
	}

	@Override
	public void shutdown() {
		combinedDownloader.shutdown();
	}

	@Override
	public boolean isShutdown() {
		return combinedDownloader.isShutdown();
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
		return combinedDownloader.download(task, callback);
	}

	@Override
	public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback) {
		return combinedDownloader.download(task, callback);
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
		return combinedDownloader.download(task, callback, tries);
	}

	@Override
	public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
		return combinedDownloader.download(task, callback, tries);
	}

	@Override
	public Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, CombinedDownloadCallback<Version> callback) {
		return download(new IncrementallyDownloadTask(downloadProvider, dir, version, checkLibrariesHash, checkAssetsHash, updateSnapshots), callback);
	}

	@Override
	public Future<RemoteVersionList> fetchRemoteVersionList(CombinedDownloadCallback<RemoteVersionList> callback) {
		return download(downloadProvider.versionList(), callback);
	}

	@Override
	public MinecraftDownloadProvider getProvider() {
		return downloadProvider;
	}

	@Override
	public String toString() {
		return String.format("MinecraftDownloaderImpl [combinedDownloader=%s, downloadProvider=%s, checkLibrariesHash=%s, checkAssetsHash=%s, updateSnapshots=%s]", combinedDownloader, downloadProvider, checkLibrariesHash, checkAssetsHash, updateSnapshots);
	}

}
