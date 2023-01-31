package org.to2mbn.jmccc.mojangapi;

import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

import java.util.UUID;

public interface MojangAPIProvider {

    String apiStatus();

    String nameHistory(UUID uuid);

    String texture(UUID uuid, TextureType type);

    String userInfo();

    String blockedServers();

    String salesStatistics();

}
