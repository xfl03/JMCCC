package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.util.Map;
import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

/**
 * Provides game profile apis.
 * 
 * @author yushijinhun
 */
public interface ProfileService {

	/**
	 * Returns the specified player's profile, or null if the player doesn't
	 * exist.
	 * <p>
	 * For Mojang implementation: This method has a strict rate limit: You can
	 * request the same profile once per minute, however, you can send as many
	 * unique requests as you like. So you'd better cache the profiles.
	 * 
	 * @param profileUUID the player's uuid
	 * @return the specified player's profile, null if the player doesn't exist
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException;

	/**
	 * Returns the specified player's textures.
	 * <p>
	 * If {@code profile} is a {@code PropertiesGameProfile}, this method won't
	 * have any network communication. Otherwise, this method will call
	 * {@link #getGameProfile(UUID)} to get the player's full profile. So you'd
	 * better cache the textures, or pass in {@code PropertiesGameProfile}.
	 * (because {@link #getGameProfile(UUID)} has a rate limit)
	 * 
	 * @param profile the player's profile
	 * @return the player's textures, can be null
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	Map<TextureType, Texture> getTextures(GameProfile profile) throws AuthenticationException;

	/**
	 * Returns the profile of the player that uses the specified username, or
	 * null if no such a player exists.
	 * 
	 * @param name the player's name
	 * @return the profile of the player that uses the specified username, or
	 *         null if no such a player exists
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	GameProfile lookupGameProfile(String name) throws AuthenticationException;

	/**
	 * Returns the profile of the player that uses the specified username at the
	 * timestamp provided, or null if no such a player exists.
	 * 
	 * @param name the player's name
	 * @param timestamp the timestamp (java timestamp)
	 * @return the profile of the player that uses the specified username at the
	 *         timestamp provided, or null if no such a player exists
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	GameProfile lookupGameProfile(String name, long timestamp) throws AuthenticationException;

}
