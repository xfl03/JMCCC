package com.github.to2mbn.jmccc.auth;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import org.json.JSONObject;
import com.github.to2mbn.jmccc.launch.AuthenticationException;
import com.github.to2mbn.jyal.Agent;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.Session;
import com.github.to2mbn.jyal.SessionService;
import com.github.to2mbn.jyal.util.UUIDUtils;
import com.github.to2mbn.jyal.yggdrasil.YggdrasilSessionService;

/**
 * Yggdrasil authenticator using token.
 * <p>
 * This class is serializable. If you want to save the authentication (aka 'remember password'), you don't need to save
 * the password, just save this YggdrasilTokenAuthenticator object. It's safer because YggdrasilTokenAuthenticator only
 * saves the access token.
 * 
 * @author yushijinhun
 */
public class YggdrasilTokenAuthenticator implements Authenticator, Externalizable {

	private static final long serialVersionUID = 1L;

	private SessionService sessionService;

	private String clientToken;
	private String accessToken;

	/**
	 * Creates a YggdrasilTokenAuthenticator with the given client token and the given access token.
	 * 
	 * @param clientToken the given client token
	 * @param accessToken the given access token
	 */
	public YggdrasilTokenAuthenticator(String clientToken, String accessToken) {
		Objects.requireNonNull(clientToken);
		Objects.requireNonNull(accessToken);
		this.clientToken = clientToken;
		this.accessToken = accessToken;
		createSessionService();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method will update the stored access token after authentication.
	 */
	@Override
	public AuthResult auth() throws AuthenticationException {
		Session session;
		try {
			session = sessionService.loginWithToken(accessToken);
		} catch (com.github.to2mbn.jyal.AuthenticationException e) {
			throw new AuthenticationException(e);
		}

		accessToken = session.getAccessToken();

		GameProfile profile = session.getSelectedGameProfile();

		String properties;
		if (session.getUserProperties() == null) {
			properties = "{}";
		} else {
			properties = new JSONObject(session.getUserProperties()).toString();
		}

		return new AuthResult(profile.getName(), session.getAccessToken(), UUIDUtils.toUnsignedUUIDString(profile.getUUID()), properties, session.getUserType().getName());
	}

	/**
	 * Checks if the access token is valid.
	 * <p>
	 * If this method returns false, you shouldn't use this YggdrasilTokenAuthenticator any longer. You need to create
	 * another YggdrasilTokenAuthenticator by password authentication.
	 * 
	 * @return true if the access token is valid
	 * @throws AuthenticationException if an error has occurred during validating
	 */
	public boolean isValid() throws AuthenticationException {
		try {
			return sessionService.isValid(accessToken);
		} catch (com.github.to2mbn.jyal.AuthenticationException e) {
			throw new AuthenticationException("failed to valid access token", e);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(clientToken);
		out.writeObject(accessToken);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientToken = in.readUTF();
		accessToken = in.readUTF();
		createSessionService();
	}

	private void createSessionService() {
		sessionService = new YggdrasilSessionService(clientToken, Agent.MINECRAFT);
	}

}
