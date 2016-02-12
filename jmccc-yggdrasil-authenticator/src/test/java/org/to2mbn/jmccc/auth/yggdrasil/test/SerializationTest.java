package org.to2mbn.jmccc.auth.yggdrasil.test;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilAuthenticationService;

public class SerializationTest {

	@Test
	public void testYggdrasilAuthenticator() throws IOException, ClassNotFoundException {
		YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator("233x233x233x233");

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream objout = new ObjectOutputStream(bout);
		objout.writeObject(authenticator);
		objout.flush();

		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream objin = new ObjectInputStream(bin);
		YggdrasilAuthenticator authenticator2 = (YggdrasilAuthenticator) objin.readObject();
		assertEquals("233x233x233x233", ((YggdrasilAuthenticationService) authenticator2.getAuthenticationService()).getClientToken());
	}

}
