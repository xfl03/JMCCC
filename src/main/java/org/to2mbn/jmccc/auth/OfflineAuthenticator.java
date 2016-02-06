package org.to2mbn.jmccc.auth;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.UUID;

public class OfflineAuthenticator implements Authenticator, Serializable {

	private static final long serialVersionUID = 1L;

	private String playerName;

	/**
	 * Creates an OfflineAuthenticator.
	 * 
	 * @param playerName the offline player name
	 * @throws NullPointerException if <code>playerName==null</code>
	 * @throws IllegalArgumentException if <code>playerName.length()==0</code>
	 */
	public OfflineAuthenticator(String playerName) {
		Objects.requireNonNull(playerName);
		this.playerName = playerName;

		if (this.playerName.length() == 0) {
			throw new IllegalArgumentException("Zero length player name");
		}
	}

	@Override
	public AuthInfo auth() throws AuthenticationException {
		try {
			return new AuthInfo(playerName, unsign(UUID.randomUUID()), unsign(generateUUID()), "{}", "mojang");
		} catch (UnsupportedEncodingException e) {
			throw new AuthenticationException("UTF-8 is not supported", e);
		}
	}

	private UUID generateUUID() throws UnsupportedEncodingException {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes("UTF-8"));
	}

	private String unsign(UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	@Override
	public String toString() {
		return "OfflineAuthenticator[" + playerName + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OfflineAuthenticator) {
			OfflineAuthenticator another = (OfflineAuthenticator) obj;
			return playerName.equals(another.playerName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return playerName.hashCode();
	}
}
