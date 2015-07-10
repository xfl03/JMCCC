package com.darkyoooooo.jmccc.auth;

import com.darkyoooooo.jmccc.util.Utils;

public class OfflineAuthenticator implements IAuthenticator {
	private String playerName;
	
	public OfflineAuthenticator(String playerName) {
		this.playerName = playerName.trim();
	}

	@Override
	public AuthInfo run() {
		if(this.playerName.isEmpty()) {
			return new AuthInfo("玩家名称不符规范");
		}
		if(this.playerName.toCharArray().length < 5) {
			return new AuthInfo("玩家名称过短");
		}
		return new AuthInfo(
			Utils.genRandomTokenOrUUID(),
			this.playerName,
			Utils.genRandomTokenOrUUID(),
			"{}",
			"mojang"
		);
	}
}
