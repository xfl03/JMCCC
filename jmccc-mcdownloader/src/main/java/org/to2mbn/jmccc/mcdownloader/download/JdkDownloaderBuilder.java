package org.to2mbn.jmccc.mcdownloader.download;

public class JdkDownloaderBuilder extends AbstractDownloaderBuilder {

	public static JdkDownloaderBuilder create() {
		return new JdkDownloaderBuilder();
	}

	public static Downloader buildDefault() {
		return create().build();
	}

	@Override
	public Downloader build() {
		return new JdkHttpDownloader(maxConnections, connectTimeout, readTimeout, downloadPoolKeepAliveTime, downloadPoolKeepAliveTimeUnit, proxy);
	}

}
