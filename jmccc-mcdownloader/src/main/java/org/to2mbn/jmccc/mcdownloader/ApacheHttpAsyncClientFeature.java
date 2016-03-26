package org.to2mbn.jmccc.mcdownloader;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;

final class ApacheHttpAsyncClientFeature {

	static DownloaderService createApacheHttpAsyncClient(Executor executor, MinecraftDownloaderBuilder builder) {
		HttpHost proxyHost = resolveProxy(builder.proxy);
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create()
				.setMaxConnTotal(builder.maxConnections)
				.setMaxConnPerRoute(builder.maxConnectionsPerRouter == 0 ? builder.maxConnections : builder.maxConnectionsPerRouter)
				.setProxy(proxyHost)
				.setDefaultIOReactorConfig(IOReactorConfig.custom()
						.setConnectTimeout(builder.connectTimeout)
						.setSoTimeout(builder.soTimeout)
						.build())
				.setDefaultRequestConfig(RequestConfig.custom()
						.setConnectTimeout(builder.connectTimeout)
						.setSocketTimeout(builder.soTimeout)
						.setProxy(proxyHost)
						.build());
		return new HttpAsyncDownloader(httpClientBuilder, executor);
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

	private ApacheHttpAsyncClientFeature() {
	}
}
