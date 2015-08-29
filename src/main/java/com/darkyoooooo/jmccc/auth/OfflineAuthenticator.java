package com.darkyoooooo.jmccc.auth;

import com.darkyoooooo.jmccc.util.Utils;

public class OfflineAuthenticator implements IAuthenticator {
    private String playerName;

    public OfflineAuthenticator(String playerName) {
        this.playerName = playerName.trim();
    }

    @Override
    public AuthInfo get() {
        if (this.playerName.trim().length() < 5) {
            return new AuthInfo("玩家名称过短");
        } else {
            return new AuthInfo(Utils.genRandomToken(), this.playerName, Utils.genRandomToken(), "{}", "mojang");
        }
    }
}
