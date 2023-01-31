package org.to2mbn.jmccc.test;

import org.junit.Test;
import org.to2mbn.jmccc.util.HexUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HexUtilsTest {

    @Test
    public void testEmptyHexToBytes() {
        assertArrayEquals(HexUtils.hexToBytes(""), new byte[0]);
    }

    @Test
    public void testEmptyBytesToHex() {
        assertEquals(HexUtils.bytesToHex(new byte[0]), "");
    }

    @Test
    public void testHexToBytes() {
        assertArrayEquals(HexUtils.hexToBytes("000c82756fd54e40cb236199f2b479629d0aca2f"), new byte[]{0, 12, -126, 117, 111, -43, 78, 64, -53, 35, 97, -103, -14, -76, 121, 98, -99, 10, -54, 47});
    }

    @Test
    public void testBytesToHex() {
        assertEquals(HexUtils.bytesToHex(new byte[]{0, 12, -126, 117, 111, -43, 78, 64, -53, 35, 97, -103, -14, -76, 121, 98, -99, 10, -54, 47}), "000c82756fd54e40cb236199f2b479629d0aca2f");
    }
}
