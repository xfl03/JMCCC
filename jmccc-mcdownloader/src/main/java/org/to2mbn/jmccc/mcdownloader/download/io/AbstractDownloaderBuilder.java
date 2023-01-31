package org.to2mbn.jmccc.mcdownloader.download.io;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.util.Builder;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

abstract public class AbstractDownloaderBuilder implements Builder<Downloader> {

    protected int maxConnections = 20;
    protected int connectTimeout = 10000;
    protected int readTimeout = 20000;
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
