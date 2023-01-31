package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.junit.Test;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class YggdrasilAuthenticatorTest {

    @Test
    public void testRefreshWithPassword() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.refreshWithPassword("user", "password");
        assertEquals(service.session(), authenticator.getCurrentSession());
        assertEquals(service.e_profiles[0], authenticator.getCurrentSession().getSelectedProfile());
    }

    @Test
    public void testRefreshWithPasswordSelectingProfile() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.refreshWithPassword("user", "password", new MockCharacterSelector("player2"));
        assertEquals(service.session(), authenticator.getCurrentSession());
        assertEquals(service.e_profiles[1], authenticator.getCurrentSession().getSelectedProfile());
    }

    public void testRefreshWithPasswordNullProfile() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.refreshWithPassword("user", "password", new MockCharacterSelector(null));
    }

    public void testRefreshWithPasswordNoProfile() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        service.e_profiles = new GameProfile[0];
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.refreshWithPassword("user", "password");
    }

    @Test
    public void testRefreshWithPasswordSelectedProfile() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        service.e_selectedProfile = service.e_profiles[1];
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.refreshWithPassword("user", "password", new MockCharacterSelector(null));
        assertEquals(service.session(), authenticator.getCurrentSession());
        assertEquals(service.e_profiles[1], authenticator.getCurrentSession().getSelectedProfile());
    }

    @Test(expected = AuthenticationException.class)
    public void testSessionNoCredential() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
        authenticator.session();
    }

    @Test
    public void testSessionPassiveRefresh() throws AuthenticationException {
        MockAuthenticationService service = new MockAuthenticationService();
        YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service) {

            private static final long serialVersionUID = 1L;

            @Override
            protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
                return createPasswordProvider("user", "password", new MockCharacterSelector("player2"));
            }

        };
        Session session1 = authenticator.session();

        assertEquals(service.session(), authenticator.getCurrentSession());
        assertEquals(service.e_profiles[1], authenticator.getCurrentSession().getSelectedProfile());

        // because the session is available, so it shouldn't be refreshed
        Session session2 = authenticator.session();
        assertEquals(session1, session2);

        // make the token unavailable, and prevent YggdrasilAuthenticator logging in with password
        // to let it refresh with token
        service.e_password = "shouldn't_login_with_password";
        service.tokenAvailable = false;
        Session session3 = authenticator.session();
        assertNotEquals(session2.getAccessToken(), session3.getAccessToken());

        // make the token unavailable
        // to let it refresh with password
        service.e_password = "password";
        service.tokenAvailable = true;
        service.e_accessToken = "invalid";
        service.e_clientToken = "invalid";
        Session session4 = authenticator.session();
        assertNotEquals(session3.getAccessToken(), session4.getAccessToken());
    }

}
