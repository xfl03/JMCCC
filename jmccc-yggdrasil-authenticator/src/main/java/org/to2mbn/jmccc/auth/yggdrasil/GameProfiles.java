package org.to2mbn.jmccc.auth.yggdrasil;

import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.PlayerTextures;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.PropertiesGameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilProfileService;

/**
 * A tool class for game profiles.
 * 
 * @author yushijinhun
 */
public final class GameProfiles {

	private static final ProfileService profileService = new YggdrasilProfileService();

	/**
	 * Gets the default yggdrasil profile service.
	 * 
	 * @return the default yggdrasil profile service
	 */
	public static ProfileService getDefaultProfileService() {
		return profileService;
	}

	/**
	 * Gets the specific game profile from the yggdrasil server.
	 * 
	 * @param profileUUID the uuid of the profile
	 * @return the game profile
	 * @throws AuthenticationException if an yggdrasil authentication error occurs
	 */
	public static PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException {
		return profileService.getGameProfile(profileUUID);
	}

	/**
	 * Fills the properties to the given game profile.
	 * <p>
	 * If <code>profile instanceof PropertiesGameProfile</code>, this method will return <code>profile</code> itself.
	 * Otherwise, this method will fetch the properties from the yggdrasil server.
	 * 
	 * @param profile the profile to fill
	 * @return the filled game profile
	 * @throws AuthenticationException if an yggdrasil authentication error occurs during fetching the properties from
	 *         the yggdrasil server
	 */
	public static PropertiesGameProfile withProperties(GameProfile profile) throws AuthenticationException {
		if (profile instanceof PropertiesGameProfile) {
			return (PropertiesGameProfile) profile;
		}
		return getGameProfile(profile.getUUID());
	}

	/**
	 * Gets the textures of the given player.
	 * <p>
	 * If <code>!(profile instanceof PropertiesGameProfile)</code>, the method will fetches the properties from the
	 * yggdrasil server.
	 * 
	 * @param profile the profile of the player
	 * @return the textures of the player
	 * @throws AuthenticationException if an yggdrasil authentication error occurs
	 */
	public static PlayerTextures getTextures(GameProfile profile) throws AuthenticationException {
		return profileService.getTextures(withProperties(profile));
	}

	/**
	 * Gets the textures of the given player.
	 * <p>
	 * This method will fetches the properties from the yggdrasil server.
	 * 
	 * @param profileUUID the profile uuid of the player
	 * @return the textures of the player
	 * @throws AuthenticationException if an yggdrasil authentication error occurs
	 */
	public static PlayerTextures getTextures(UUID profileUUID) throws AuthenticationException {
		return profileService.getTextures(getGameProfile(profileUUID));
	}

	private GameProfiles() {
	}

}
