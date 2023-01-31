package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

@RunWith(Parameterized.class)
public class Base64Test {

    private byte[] source;
    private char[] encoded;
    public Base64Test(byte[] source, char[] encoded) {
        this.source = source;
        this.encoded = encoded;
    }

    @Parameters
    public static List<Object[]> data() throws UnsupportedEncodingException {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{new byte[0], new char[0]});
        data.add(new Object[]{"1".getBytes("ASCII"), "MQ==".toCharArray()});
        data.add(new Object[]{"1\n".getBytes("ASCII"), "MQo=".toCharArray()});
        data.add(new Object[]{"啊哈哈哈哈哈\n".getBytes("UTF-8"), "5ZWK5ZOI5ZOI5ZOI5ZOI5ZOICg==".toCharArray()});
        data.add(new Object[]{"test\ntset".getBytes("ASCII"), "dGVzdA\np0c2V0".toCharArray()});
        return data;
    }

    @Test
    public void testEncode() {
        assertArrayEquals(deleteLineChars(encoded), Base64.encode(source));
    }

    @Test
    public void testDecode() {
        assertArrayEquals(source, Base64.decode(encoded));
    }

    private char[] deleteLineChars(char[] in) {
        int length = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != '\n' && in[i] != '\r') {
                length++;
            }
        }
        char[] out = new char[length];
        int l = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != '\n' && in[i] != '\r') {
                out[l++] = in[i];
            }
        }
        return out;
    }

}
