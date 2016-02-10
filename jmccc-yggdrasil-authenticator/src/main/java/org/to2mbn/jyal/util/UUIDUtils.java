package org.to2mbn.jyal.util;

import java.util.UUID;

public final class UUIDUtils {

	public static String unsign(UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	public static UUID fromString(String uuid) {
		if (uuid.length() == 36) {
			return UUID.fromString(uuid);
		} else {
			return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32));
		}
	}

	private UUIDUtils() {
	}

}
