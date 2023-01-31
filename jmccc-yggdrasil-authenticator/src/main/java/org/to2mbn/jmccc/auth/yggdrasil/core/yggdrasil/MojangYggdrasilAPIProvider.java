package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.util.UUIDUtils;

import java.io.Serializable;
import java.util.UUID;

public class MojangYggdrasilAPIProvider implements YggdrasilAPIProvider, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String authenticate() {
        return "https://authserver.mojang.com/authenticate";
    }

    @Override
    public String refresh() {
        return "https://authserver.mojang.com/refresh";
    }

    @Override
    public String validate() {
        return "https://authserver.mojang.com/validate";
    }

    @Override
    public String invalidate() {
        return "https://authserver.mojang.com/invalidate";
    }

    @Override
    public String signout() {
        return "https://authserver.mojang.com/signout";
    }

    @Override
    public String profile(UUID profileUUID) {
        return "https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDUtils.unsign(profileUUID);
    }

    @Override
    public String profileByUsername(String username) {
        return "https://api.mojang.com/users/profiles/minecraft/" + username;
    }

    @Override
    public String profilesLookup() {
        return "https://api.mojang.com/profiles/minecraft";
    }

}
