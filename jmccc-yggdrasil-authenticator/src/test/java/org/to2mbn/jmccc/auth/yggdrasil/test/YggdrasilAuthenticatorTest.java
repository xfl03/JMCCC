package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.junit.Test;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;
import static org.junit.Assert.*;

public class YggdrasilAuthenticatorTest {

	@Test
	public void testRefreshWithPassword() throws AuthenticationException {
		MockAuthenticationService service = new MockAuthenticationService();
		YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator(service);
		authenticator.refreshWithPassword("user", "password");
		assertEquals(service.session(), authenticator.getCurrentSession());
	}

}
