package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;

public interface ProfileService {

	PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException;

	PlayerTextures getTextures(GameProfile profile) throws AuthenticationException;

	UUID lookupUUIDByName(String playerName) throws AuthenticationException;

}
