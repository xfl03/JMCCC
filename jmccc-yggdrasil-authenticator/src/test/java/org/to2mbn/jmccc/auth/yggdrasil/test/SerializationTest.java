package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.junit.Test;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;

import java.io.*;

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
