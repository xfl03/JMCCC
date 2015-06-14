package com.darkyoooooo.jmccc.auth;

import lombok.Getter;
import net.kronos.mclaunch_util_lib.auth.YggdrasilRequester;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAgent;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAuthenticateRes;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilProfile;

import com.darkyoooooo.jmccc.util.Utils;

public class YggdrasilAuthenticator implements IAuthenticator {
	@Getter private String email, password, clientToken;
	@Getter private boolean enableTwitch;
	
	public YggdrasilAuthenticator(String email, String password, boolean enableTwitch,
			String clientToken) {
		this.email = email;
		this.password = password;
		this.enableTwitch = enableTwitch;
		this.clientToken = clientToken;
	}
	
	public YggdrasilAuthenticator(String email, String password, boolean enableTwitch) {
		this(email, password, enableTwitch, Utils.genRandomTokenOrUUID());
	}

	@Override
	public String getType() {
		return "JMCCC.Yggdrasil";
	}

	@Override
	public AuthInfo run() {
		YggdrasilRequester req = new YggdrasilRequester();
		YggdrasilAuthenticateRes res;
		try {
			res = req.authenticate(YggdrasilAgent.getMinecraftAgent(), this.email, this.password);
			YggdrasilProfile profile = res.getSelectedProfile();
			return new AuthInfo(profile.getId(), profile.getName(), res.getAccessToken(), "{}",
				null, profile.isLegacy() ? "legacy" : "mojang");
		} catch(Throwable t) {
			return new AuthInfo(null, null, null, null, "验证失败", null);
		}
	}
}
