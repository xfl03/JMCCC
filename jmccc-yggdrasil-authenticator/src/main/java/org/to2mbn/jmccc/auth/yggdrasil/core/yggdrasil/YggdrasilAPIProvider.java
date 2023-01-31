package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.util.UUID;

public interface YggdrasilAPIProvider {

    String authenticate();

    String refresh();

    String validate();

    String invalidate();

    String signout();

    String profile(UUID profileUUID);

    String profileByUsername(String username);

    String profilesLookup();

}
