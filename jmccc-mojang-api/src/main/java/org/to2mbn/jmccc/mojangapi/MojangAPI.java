package org.to2mbn.jmccc.mojangapi;

import java.util.Map;
import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.SessionCredential;
import org.to2mbn.jmccc.auth.yggdrasil.core.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.TextureType;

public interface MojangAPI {

	/**
	 * Returns status of various Mojang services.
	 * 
	 * @return the status of Mojang services
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	Map<String, ServiceStatus> getServiceStatus() throws AuthenticationException;

	/**
	 * Returns the name history of the specified character.
	 * <p>
	 * The array is sorted by time. The first element is the character's first
	 * username. And the last element is the current username.
	 * 
	 * @param uuid the character's uuid
	 * @return the name history of the specified character
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	FormerName[] getNameHistory(UUID uuid) throws AuthenticationException;

	/**
	 * Sets the character's texture.
	 * <p>
	 * If {@code texture} is {@code null}, this method will reset the texture.
	 * 
	 * @param credential the account's credential
	 * @param uuid the character's uuid
	 * @param type the type of the texture
	 * @param texture the texture, can be null
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	void setTexture(SessionCredential credential, UUID uuid, TextureType type, Texture texture) throws AuthenticationException;

	/**
	 * Gets the information of the account.
	 * 
	 * @param credential the account's credential
	 * @return the information of the account
	 * @throws AuthenticationException if an exception occurred during
	 *             requesting
	 */
	AccountInfo getAccountInfo(SessionCredential credential) throws AuthenticationException;

}
