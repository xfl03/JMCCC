package org.to2mbn.jmccc.util;

public final class HexUtils {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private HexUtils() {
    }

    public static String bytesToHex(byte[] bytes) {
        char[] str = new char[bytes.length << 1];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            str[j++] = HEX[(bytes[i] & 0xf0) >>> 4];
            str[j++] = HEX[bytes[i] & 0x0f];
        }
        return new String(str);
    }

    public static byte[] hexToBytes(String hex) {
        char[] chars = hex.toLowerCase().toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((hexCharToByte(chars[j++]) << 4) | hexCharToByte(chars[j++]));
        }
        return bytes;
    }

    private static byte hexCharToByte(char hexChar) {
        int result = Character.digit(hexChar, 16);
        if (result == -1) {
            throw new IllegalArgumentException("invalid hexadecimal character: " + hexChar);
        }
        return (byte) result;
    }

}
