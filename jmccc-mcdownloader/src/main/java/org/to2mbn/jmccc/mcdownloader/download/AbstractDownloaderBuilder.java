package org.to2mbn.jmccc.mcdownloader.download;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import org.to2mbn.jmccc.util.Builder;

abstract public class AbstractDownloaderBuilder implements Builder<Downloader> {

	protected int maxConnections;
	protected int connectTimeout;
	protected int readTimeout;
	protected Proxy proxy = Proxy.NO_PROXY;
	protected long downloadPoolKeepAliveTime = 10;
	protected TimeUnit downloadPoolKeepAliveTimeUnit = TimeUnit.SECONDS;

	protected AbstractDownloaderBuilder() {
	}

	public AbstractDownloaderBuilder maxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		return this;
	}

	public AbstractDownloaderBuilder connectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public AbstractDownloaderBuilder readTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	public AbstractDownloaderBuilder proxy(Proxy proxy) {
		this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
		return this;
	}

	public AbstractDownloaderBuilder downloadPoolKeepAliveTime(long downloadPoolKeepAliveTime, TimeUnit unit) {
		this.downloadPoolKeepAliveTime = downloadPoolKeepAliveTime;
		this.downloadPoolKeepAliveTimeUnit = unit;
		return this;
	}

}
