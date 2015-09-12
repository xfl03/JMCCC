package com.github.to2mbn.jyal.util;

import java.util.UUID;

public final class UUIDUtils {

	public static String toUnsignedUUIDString(UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	public static UUID fromUUIDString(String uuid) {
		if (uuid.length() == 32) {
			return UUID.fromString(uuid);
		} else {
			return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 4) + "-" + uuid.substring(12, 4) + "-" + uuid.substring(16, 4) + "-" + uuid.substring(20, 12));
		}
	}

	private UUIDUtils() {
	}

}
