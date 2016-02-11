package org.to2mbn.jmccc.auth.yggdrasil;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.UUIDUtils;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilAuthenticationService;
import org.to2mbn.jmccc.auth.AuthenticationException;

public class YggdrasilAuthenticator implements Authenticator, Serializable {

	public static interface PasswordProvider {

		String getUsername() throws AuthenticationException;

		String getPassword() throws AuthenticationException;

		CharacterSelector getCharacterSelector();

	}

	private static final long serialVersionUID = 1L;

	private AuthenticationService sessionService;
	private volatile Session authResult;

	public YggdrasilAuthenticator() {
		this(UUIDUtils.unsign(UUID.randomUUID()));
	}

	public YggdrasilAuthenticator(String clientToken) {
		this(new YggdrasilAuthenticationService(clientToken, Agent.MINECRAFT));
	}

	public YggdrasilAuthenticator(AuthenticationService sessionService) {
		Objects.requireNonNull(sessionService);
		this.sessionService = sessionService;
	}

	@Override
	public synchronized AuthInfo auth() throws AuthenticationException {
		GameProfile selectedProfile = session().getSelectedProfile();
		if (selectedProfile == null) {
			throw new AuthenticationException("no profile is available");
		}
		return new AuthInfo(selectedProfile.getName(), authResult.getAccessToken(), UUIDUtils.unsign(selectedProfile.getUUID()), new JSONObject(authResult.getProperties()).toString(), authResult.getUserType().getName());
	}

	public synchronized Session session() throws AuthenticationException {
		if (authResult == null || !sessionService.validate(authResult.getAccessToken())) {
			refresh();
		}
		if (authResult == null) {
			throw new AuthenticationException("no authentication is available");
		}
		return authResult;
	}

	public synchronized void refresh() throws AuthenticationException {
		if (authResult == null) {
			// refresh operation is not available
			PasswordProvider passwordProvider = tryPasswordLogin();
			if (passwordProvider == null) {
				throw new AuthenticationException("no more authentication methods to try");
			}
			refreshWithPassword(passwordProvider);
		} else {
			try {
				refreshWithToken(authResult.getAccessToken());
			} catch (AuthenticationException e) {
				// token login failed
				PasswordProvider passwordProvider = tryPasswordLogin();
				if (passwordProvider == null) {
					throw e;
				}

				try {
					refreshWithPassword(passwordProvider);
				} catch (AuthenticationException e1) {
					e1.addSuppressed(e);
					throw e1;
				}
			}
		}
	}

	public synchronized void refreshWithPassword(PasswordProvider passwordProvider) throws AuthenticationException {
		Objects.requireNonNull(passwordProvider);
		authResult = sessionService.login(passwordProvider.getUsername(), passwordProvider.getPassword());
		if (authResult.getSelectedProfile() == null) {
			// no profile is selected
			// let's select one
			CharacterSelector selector = passwordProvider.getCharacterSelector();
			if (selector == null) {
				selector = new DefaultCharacterSelector();
			}
			GameProfile[] profiles = authResult.getProfiles();
			if (profiles == null || profiles.length == 0) {
				throw new AuthenticationException("no profile is available");
			}
			GameProfile selectedProfile = selector.select(profiles);
			authResult = sessionService.selectProfile(authResult.getAccessToken(), selectedProfile.getUUID());
		}
	}

	public synchronized void refreshWithToken(String accessToken) throws AuthenticationException {
		Objects.requireNonNull(accessToken);
		authResult = sessionService.refresh(accessToken);
	}

	public synchronized void clearToken() {
		authResult = null;
	}

	public synchronized Session getCurrentSession() {
		return authResult;
	}

	public synchronized void setCurrentSession(Session session) {
		this.authResult = session;
	}

	protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
		return null;
	}

}
