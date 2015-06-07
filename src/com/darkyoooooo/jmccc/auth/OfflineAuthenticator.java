package com.darkyoooooo.jmccc.auth;

import lombok.Getter;

import com.darkyoooooo.jmccc.util.Utils;

public class OfflineAuthenticator implements IAuthenticator {
	@Getter private final String playerName;
	
	public OfflineAuthenticator(String playerName) {
		this.playerName = playerName.trim();
	}
	
	@Override
	public String getType() {
		return "JMCCC.Offline";
	}

	@Override
	public AuthInfo run() {
		if(this.playerName.isEmpty()) {
			return new AuthInfo(
				null, null, null, null,
				"玩家名称不符规范",
				null
			);
		}
		return new AuthInfo(
			Utils.genRandomTokenOrUUID(),
			this.playerName,
			Utils.genRandomTokenOrUUID(),
			"{}",
			null,
			"Mojang"
		);
	}
}
