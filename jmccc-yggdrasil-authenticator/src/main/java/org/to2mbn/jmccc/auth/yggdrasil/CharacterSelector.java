package org.to2mbn.jmccc.auth.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;

public interface CharacterSelector {

	/**
	 * Selects one of the given characters to login.
	 * <p>
	 * This method may be called during the authentication if no character is selected. An
	 * {@link AuthenticationException} will be thrown if the method returns <code>null</code>.
	 * 
	 * @param availableProfiles the available characters
	 * @return the character to login
	 */
	GameProfile select(GameProfile[] availableProfiles);

}
