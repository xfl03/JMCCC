package org.to2mbn.jyal;

import java.util.UUID;

public interface ProfileService {

	PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException;

	PlayerTextures getTextures(PropertiesGameProfile profile) throws AuthenticationException;

}
