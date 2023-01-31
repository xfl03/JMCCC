package org.to2mbn.jmccc.auth.yggdrasil.core.util;

public final class Base64 {

    private static final char[] ENCODE_TABLE = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '='};

    private static final byte[] DECODE_TABLE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    private Base64() {
    }

    public static byte[] decode(char[] data) {
        data = deleteLineChars(data);
        int length = ((data.length + 3) / 4) * 3;
        if (data.length > 0 && data[data.length - 1] == '=') {
            length--;
        }
        if (data.length > 1 && data[data.length - 2] == '=') {
            length--;
        }
        byte[] out = new byte[length];
        int shift = 0;
        int accum = 0;
        int index = 0;
        for (int ix = 0; ix < data.length; ix++) {
            int value = DECODE_TABLE[data[ix] & 0xff];
            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;
                if (shift >= 8) {
                    shift -= 8;
                    out[index++] = (byte) ((accum >> shift) & 0xff);
                }
            }
        }
        return out;
    }

    static public char[] encode(byte[] data) {
        char[] out = new char[((data.length + 2) / 3) * 4];

        for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;
            int val = (0xff & data[i]);
            val <<= 8;
            if ((i + 1) < data.length) {
                val |= (0xff & data[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < data.length) {
                val |= (0xff & data[i + 2]);
                quad = true;
            }
            out[index + 3] = ENCODE_TABLE[(quad ? (val & 0x3f) : 63)];
            val >>= 6;
            out[index + 2] = ENCODE_TABLE[(trip ? (val & 0x3f) : 63)];
            val >>= 6;
            out[index + 1] = ENCODE_TABLE[val & 0x3f];
            val >>= 6;
            out[index + 0] = ENCODE_TABLE[val & 0x3f];
        }
        return out;
    }

    private static char[] deleteLineChars(char[] in) {
        int length = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != '\n' && in[i] != '\r') {
                length++;
            }
        }
        if (length == in.length) {
            return in;
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
