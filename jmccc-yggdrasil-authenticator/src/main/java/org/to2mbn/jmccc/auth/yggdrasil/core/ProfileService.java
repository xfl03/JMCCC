package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
     * @throws AuthenticationException if an exception occurs during requesting
     */
    PropertiesGameProfile getGameProfile(UUID profileUUID) throws AuthenticationException;

    /**
     * Fetches the properties of the profile and returns it as a
     * {@code PropertiesGameProfile}. If the profile is already a
     * {@code PropertiesGameProfile}, nothing will be done.
     * <p>
     * This method has a rate limit, see {@link #getGameProfile(UUID)}.
     *
     * @param profile the profile
     * @return the profile with properties
     * @throws AuthenticationException if an exception occurs during requesting
     */
    PropertiesGameProfile fillProperties(GameProfile profile) throws AuthenticationException;

    /**
     * Returns the specified player's textures.
     *
     * @param profile the player's profile
     * @return the player's textures, can be null
     * @throws AuthenticationException if an exception occurs during requesting
     */
    Map<TextureType, Texture> getTextures(PropertiesGameProfile profile) throws AuthenticationException;

    /**
     * Returns the profile of the player that uses the specified username, or
     * null if no such a player exists.
     *
     * @param name the player's name
     * @return the profile of the player that uses the specified username, or
     * null if no such a player exists
     * @throws AuthenticationException if an exception occurs during requesting
     */
    GameProfile lookupGameProfile(String name) throws AuthenticationException;

    /**
     * Returns the profile of the player that uses the specified username at the
     * timestamp provided, or null if no such a player exists.
     *
     * @param name      the player's name
     * @param timestamp the timestamp (java timestamp)
     * @return the profile of the player that uses the specified username at the
     * timestamp provided, or null if no such a player exists
     * @throws AuthenticationException if an exception occurs during requesting
     */
    GameProfile lookupGameProfile(String name, long timestamp) throws AuthenticationException;

    /**
     * Queries the profiles of the given players. The method will block until
     * all the profiles are queried. Once a profile is queried, {@code callback}
     * will be called.
     *
     * @param names    the players' names
     * @param callback the callback
     */
    void lookupGameProfiles(Set<String> names, GameProfileCallback callback);

}
