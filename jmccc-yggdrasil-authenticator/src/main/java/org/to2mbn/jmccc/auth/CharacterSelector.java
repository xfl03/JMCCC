package org.to2mbn.jmccc.auth;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jyal.GameProfile;

public interface CharacterSelector {

	/**
	 * Selects one of the given characters to login.
	 * <p>
	 * This method will be called during the authentication. An {@link AuthenticationException} will occur if this
	 * method returns <code>null</code>.
	 * 
	 * @param selected the default character
	 * @param availableProfiles the available characters
	 * @return the character to login
	 */
	GameProfile select(GameProfile selected, GameProfile[] availableProfiles);

}
