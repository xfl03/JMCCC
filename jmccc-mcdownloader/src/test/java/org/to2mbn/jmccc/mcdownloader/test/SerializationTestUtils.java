package org.to2mbn.jmccc.mcdownloader.test;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationTestUtils {

	@SuppressWarnings("unchecked")
	public static <T> T serializeAndDeserialize(T obj) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
			out.writeObject(obj);
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
			return (T) in.readObject();
		}
	}

	public static void testSerialization(Object obj) throws ClassNotFoundException, IOException {
		assertEquals(obj, serializeAndDeserialize(obj));
	}
}
