package org.to2mbn.jmccc.mcdownloader.download.io.async;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.io.AbstractDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.util.ThreadPoolUtils;
import org.to2mbn.jmccc.util.Builder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class HttpAsyncDownloaderBuilder extends AbstractDownloaderBuilder {

    protected Builder<CloseableHttpAsyncClient> httpClient;
    protected int bootstrapPoolSize = Runtime.getRuntime().availableProcessors();

    public static boolean isAvailable() {
        try {
            Class.forName("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static HttpAsyncDownloaderBuilder create() {
        return new HttpAsyncDownloaderBuilder();
    }

    public static Downloader buildDefault() {
        return create().build();
    }

    private static HttpHost resolveProxy(Proxy proxy) {
        if (proxy.type() == Proxy.Type.DIRECT) {
            return null;
        }
        if (proxy.type() == Proxy.Type.HTTP) {
            SocketAddress socketAddress = proxy.address();
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                return new HttpHost(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            }
        }
        throw new IllegalArgumentException("Proxy '" + proxy + "' is not supported");
    }

    public HttpAsyncDownloaderBuilder httpClient(Builder<CloseableHttpAsyncClient> httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public HttpAsyncDownloaderBuilder httpClient(HttpAsyncClientBuilder httpClient) {
        this.httpClient = httpClient == null ? null : new HttpAsyncClientBuilderAdapter(httpClient);
        return this;
    }

    public HttpAsyncDownloaderBuilder bootstrapPoolSize(int bootstrapPoolSize) {
        this.bootstrapPoolSize = bootstrapPoolSize;
        return this;
    }

    @Override
    public Downloader build() {
        CloseableHttpAsyncClient client = null;
        ExecutorService pool = null;
        try {
            if (httpClient == null) {
                client = buildDefaultHttpAsyncClient();
            } else {
                client = httpClient.build();
            }

            pool = ThreadPoolUtils.createPool(bootstrapPoolSize, downloadPoolKeepAliveTime, downloadPoolKeepAliveTimeUnit, "asyncDownloader.bootstrap");
            return new HttpAsyncDownloader(client, pool);
        } catch (Throwable e) {
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            if (pool != null) {
                try {
                    pool.shutdownNow();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            throw e;
        }
    }

    protected CloseableHttpAsyncClient buildDefaultHttpAsyncClient() {
        HttpHost httpProxy = resolveProxy(proxy);
        return HttpAsyncClientBuilder.create()
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnections)
                .setProxy(httpProxy)
                .setDefaultIOReactorConfig(IOReactorConfig.custom()
                        .setConnectTimeout(connectTimeout)
                        .setSoTimeout(readTimeout)
                        .build())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(readTimeout)
                        .setProxy(httpProxy)
                        .build())
                .setDefaultHeaders(Arrays.asList(new BasicHeader("Accept-Encoding", "gzip")))
                .build();
    }

    private static class HttpAsyncClientBuilderAdapter implements Builder<CloseableHttpAsyncClient> {

        private HttpAsyncClientBuilder adapted;

        public HttpAsyncClientBuilderAdapter(HttpAsyncClientBuilder adapted) {
            this.adapted = adapted;
        }

        @Override
        public CloseableHttpAsyncClient build() {
            return adapted.build();
        }

    }

}
