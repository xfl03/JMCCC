package org.to2mbn.jmccc.mojangapi;

import java.util.Map;
import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.SessionCredential;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

/**
 * The Mojang API client.
 * <p>
 * For further information, see <a href="http://wiki.vg/Mojang_API">Mojang API -
 * wiki.vg</a>
 * <p>
 * If secured questions are enabled, there may be some problems. The following
 * exception may be thrown:
 * 
 * <pre>
 * org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException: Forbidden: Current IP not secured
 * </pre>
 * 
 * This means you haven't answered the questions, so you are not authorized to
 * access the api. To resolve this problem, you need to answer these questions
 * on the current computer, then your ip will be authorized.<br>
 * You can do the following in a web view:
 * 
 * <pre>
 * {@code POST https://account.mojang.com/login}
 * </pre>
 * 
 * With the form data:
 * 
 * <pre>
 * authenticityToken=[an random 40-characters hex string]
 * username=[email]
 * password=[password]
 * </pre>
 * 
 * Then Mojang will ask you some questions. Once the questions are correctly
 * answered (the web view jumps to {@code https://account.mojang.com/me}), your
 * ip will be authorized.
 * 
 * @author yushijinhun
 */
public interface MojangAPI {

	/**
	 * Returns status of various Mojang services.
	 * 
	 * @return the status of Mojang services
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	Map<String, ServiceStatus> getServiceStatus() throws AuthenticationException;

	/**
	 * Returns the name history of the specified player.
	 * <p>
	 * The array is sorted by time. The first element is the player's first
	 * username. And the last element is the current username.
	 * 
	 * @param uuid the player's uuid
	 * @return the name history of the specified player
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	FormerName[] getNameHistory(UUID uuid) throws AuthenticationException;

	/**
	 * Sets the player's texture.
	 * <p>
	 * If {@code texture} is {@code null}, this method will reset the texture.
	 * 
	 * @param credential the account's credential
	 * @param uuid the player's uuid
	 * @param type the type of the texture
	 * @param texture the texture, can be null
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	void setTexture(SessionCredential credential, UUID uuid, TextureType type, Texture texture) throws AuthenticationException;

	/**
	 * Gets the information of the account.
	 * 
	 * @param credential the account's credential
	 * @return the information of the account
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	AccountInfo getAccountInfo(SessionCredential credential) throws AuthenticationException;

	/**
	 * Gets the blocked server list.
	 * <p>
	 * For further information, see <a href=
	 * "https://www.reddit.com/r/Minecraft/comments/4h3c6u/mojang_is_blocking_certain_servers_as_of_193_r2/">
	 * Mojang is blocking certain servers as of 1.9.3 r2. - Reddit</a>
	 * 
	 * @return the blocked server list
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	BlockedServerList getBlockedServerList() throws AuthenticationException;

	/**
	 * Queries the statistics on the sales.
	 * <p>
	 * Valid metric keys are defined in {@link SalesStatistics.MetricKeys}.
	 * 
	 * @param metricKeys the metric keys
	 * @return the statistics on the sales
	 * @throws AuthenticationException if an exception occurs during requesting
	 */
	SalesStatistics querySales(String... metricKeys) throws AuthenticationException;

}
