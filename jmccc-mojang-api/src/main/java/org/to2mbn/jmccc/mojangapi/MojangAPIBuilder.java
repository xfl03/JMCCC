package org.to2mbn.jmccc.mojangapi;

import java.net.Proxy;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.util.Builder;

public class MojangAPIBuilder implements Builder<MojangAPI> {

	public static MojangAPIBuilder create() {
		return new MojangAPIBuilder();
	}

	public static MojangAPI buildDefault() {
		return create().build();
	}

	private MojangAPIProvider apiProvider;
	private Proxy proxy;

	public MojangAPIBuilder apiProvider(MojangAPIProvider apiProvider) {
		this.apiProvider = apiProvider;
		return this;
	}

	public MojangAPIBuilder proxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	protected MojangAPIProvider buildAPIProvider() {
		return apiProvider == null ? new DefaultMojangAPIProvider() : apiProvider;
	}

	protected HttpRequester buildHttpRequester() {
		return new HttpRequester(proxy == null ? Proxy.NO_PROXY : proxy);
	}

	@Override
	public MojangAPI build() {
		return new MojangAPIImpl(buildHttpRequester(), buildAPIProvider());
	}

}
