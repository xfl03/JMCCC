package com.github.to2mbn.jmccc.mcdownloader;

import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadContext;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;

public class RemoteVersionListDownloadTask implements MultipleDownloadTask<RemoteVersionList> {

	private MinecraftDownloadProvider downloadProvider;

	public RemoteVersionListDownloadTask(MinecraftDownloadProvider downloadProvider) {
		this.downloadProvider = downloadProvider;
	}

	@Override
	public void execute(final MultipleDownloadContext<RemoteVersionList> context) throws Exception {
		context.submit(downloadProvider.versionList(), new DownloadCallback<RemoteVersionList>() {

			@Override
			public void done(RemoteVersionList result) {
				context.done(result);
			}

			@Override
			public void failed(Throwable e) {
				context.failed(e);
			}

			@Override
			public void cancelled() {
				context.cancelled();
			}

			@Override
			public void updateProgress(long done, long total) {
			}

			@Override
			public void retry(Throwable e, int current, int max) {
			}
		}, true);
	}

}
