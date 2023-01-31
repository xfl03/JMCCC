package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.AbstractClientService;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.util.UUIDUtils;

abstract class AbstractYggdrasilService extends AbstractClientService {

    protected final PropertiesDeserializer propertiesDeserializer;
    protected final YggdrasilAPIProvider api;

    public AbstractYggdrasilService(HttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
        super(requester);
        this.propertiesDeserializer = propertiesDeserializer;
        this.api = api;
    }

    protected GameProfile parseGameProfile(JSONObject json) {
        if (json == null) {
            return null;
        }

        return new GameProfile(UUIDUtils.toUUID(json.getString("id")), json.getString("name"));
    }

}
