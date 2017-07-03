package org.to2mbn.jmccc.mcdownloader.download.io;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;

public class JdkDownloaderBuilder extends AbstractDownloaderBuilder {

	public static JdkDownloaderBuilder create() {
		return new JdkDownloaderBuilder();
	}

	public static Downloader buildDefault() {
		return create().get();
	}

	@Override
	public Downloader get() {
		return new JdkHttpDownloader(maxConnections, connectTimeout, readTimeout, downloadPoolKeepAliveTime, downloadPoolKeepAliveTimeUnit, proxy);
	}

}
