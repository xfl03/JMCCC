package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;

public interface GameProfileCallback {

    void completed(GameProfile profile);

    void failed(String name, AuthenticationException e);

}
