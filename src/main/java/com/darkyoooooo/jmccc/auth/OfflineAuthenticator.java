package com.darkyoooooo.jmccc.auth;

import java.io.UnsupportedEncodingException;

import com.darkyoooooo.jmccc.util.Utils;

public class OfflineAuthenticator implements IAuthenticator {
    private String playerName;

    public OfflineAuthenticator(String playerName) {
        this.playerName = playerName.trim();
    }

    @Override
    public AuthInfo get() {
        if (this.playerName.length() < 5) {
            return new AuthInfo("玩家名称过短");
        } else {
            try {
                return new AuthInfo(Utils.genRandomToken(this.playerName), this.playerName, Utils.genRandomToken(this.playerName), "{}", "mojang");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new AuthInfo("未知错误");
    }
}
