package com.github.to2mbn.jmccc.auth;

import com.github.to2mbn.jmccc.launch.AuthenticationException;
import com.github.to2mbn.jyal.GameProfile;

public interface CharacterSelector {

	/**
	 * Selects a character from the given characters.
	 * <p>
	 * If this method returns <code>null</code>, an {@link AuthenticationException} will occur.
	 * 
	 * @param selected the default character that mojang specified
	 * @param availableProfiles all the available characters
	 * @return the character to select
	 */
	GameProfile select(GameProfile selected, GameProfile[] availableProfiles);

}
