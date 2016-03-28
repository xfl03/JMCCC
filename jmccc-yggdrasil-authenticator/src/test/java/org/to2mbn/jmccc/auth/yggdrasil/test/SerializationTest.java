package org.to2mbn.jmccc.auth.yggdrasil.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;

public class SerializationTest {

	@Test
	public void testYggdrasilAuthenticator() throws IOException, ClassNotFoundException {
		YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator();

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream objout = new ObjectOutputStream(bout);
		objout.writeObject(authenticator);
		objout.flush();

		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream objin = new ObjectInputStream(bin);
		objin.readObject();
	}

}
