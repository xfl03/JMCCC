package com.github.to2mbn.jmccc.mcdownloader.util;

public final class HexUtils {

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] str = new char[bytes.length * 2];
		int pos;
		for (int i = 0; i < bytes.length; i++) {
			pos = i * 2;
			str[pos] = HEX[(bytes[i] & 0xf0) >>> 4];
			str[pos + 1] = HEX[bytes[i] & 0xf];
		}
		return new String(str);
	}

	public static byte[] hexToBytes(String hex) {
		char[] chars = hex.toLowerCase().toCharArray();
		byte[] bytes = new byte[chars.length / 2];
		for (int i = 0; i < bytes.length; i++) {
			int pos = i * 2;
			bytes[i] = (byte) ((hexCharToByte(chars[pos]) << 4) | hexCharToByte(chars[pos + 1]));
		}
		return bytes;
	}

	private static byte hexCharToByte(char hexChar) {
		if (hexChar >= '0' && hexChar <= '9') {
			return (byte) (hexChar - '0');
		} else if (hexChar >= 'a' && hexChar <= 'z') {
			return (byte) ((hexChar - 'a') + 10);
		} else if (hexChar >= 'A' && hexChar <= 'Z') {
			return (byte) ((hexChar - 'A') + 10);
		}
		throw new IllegalArgumentException("not a hex char: " + hexChar);
	}

	private HexUtils() {
	}

}
