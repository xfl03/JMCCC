package org.to2mbn.jmccc.mojangapi;

import java.io.Serializable;
import java.util.UUID;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.jmccc.util.UUIDUtils;

public class DefaultMojangAPIProvider implements MojangAPIProvider, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String apiStatus() {
		return "https://status.mojang.com/check";
	}

	@Override
	public String nameHistory(UUID uuid) {
		return "https://api.mojang.com/user/profiles/" + UUIDUtils.unsign(uuid) + "/names";
	}

	@Override
	public String texture(UUID uuid, TextureType type) {
		return "https://api.mojang.com/user/profile/" + UUIDUtils.unsign(uuid) + "/" + type.name().toLowerCase();
	}

	@Override
	public String userInfo() {
		return "https://api.mojang.com/user";
	}

	@Override
	public String blockedServers() {
		return "https://sessionserver.mojang.com/blockedservers";
	}

	@Override
	public String salesStatistics() {
		return "https://api.mojang.com/orders/statistics";
	}

}
