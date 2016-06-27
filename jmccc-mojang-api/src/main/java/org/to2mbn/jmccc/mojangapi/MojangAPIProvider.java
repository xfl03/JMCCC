package org.to2mbn.jmccc.mojangapi;

import java.util.UUID;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

public interface MojangAPIProvider {

	String apiStatus();

	String nameHistory(UUID uuid);

	String texture(UUID uuid, TextureType type);

	String userInfo();

	String blockedServers();

	String salesStatistics();

}
