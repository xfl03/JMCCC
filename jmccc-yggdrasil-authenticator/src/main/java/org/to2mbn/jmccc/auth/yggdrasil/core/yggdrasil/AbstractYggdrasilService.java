package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.io.AbstractClientService;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;

abstract public class AbstractYggdrasilService extends AbstractClientService {

	protected final PropertiesDeserializer propertiesDeserializer;
	protected final YggdrasilAPIProvider api;

	public AbstractYggdrasilService(JSONHttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
		super(requester);
		this.propertiesDeserializer = propertiesDeserializer;
		this.api = api;
	}

}
