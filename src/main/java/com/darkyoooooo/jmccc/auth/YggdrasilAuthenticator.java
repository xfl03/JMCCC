package com.darkyoooooo.jmccc.auth;

import net.kronos.mclaunch_util_lib.auth.YggdrasilRequester;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAgent;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAuthenticateRes;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilProfile;

public class YggdrasilAuthenticator implements IAuthenticator {
	private String email, password;
	
	public YggdrasilAuthenticator(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	@Override
	public AuthInfo run() {
		YggdrasilRequester req = new YggdrasilRequester();
		YggdrasilAuthenticateRes res;
		try {
			res = req.authenticate(YggdrasilAgent.getMinecraftAgent(), this.email, this.password);
			YggdrasilProfile profile = res.getSelectedProfile();
			return new AuthInfo(profile.getId(), profile.getName(), res.getAccessToken(), "{}",
				profile.isLegacy() ? "legacy" : "mojang");
		} catch(Throwable t) {
			return new AuthInfo("验证失败");
		}
	}
}
