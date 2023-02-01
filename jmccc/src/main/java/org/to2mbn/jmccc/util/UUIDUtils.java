package org.to2mbn.jmccc.util;

import java.util.Base64;
import java.util.UUID;

public final class UUIDUtils {

    private UUIDUtils() {
    }

    public static String unsign(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public static String unsign(String uuid) {
        return uuid.replace("-", "");
    }

    public static UUID toUUID(String uuid) {
        switch (uuid.length()) {
            case 36:
                return UUID.fromString(uuid);

            case 32:
                return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32));

            default:
                throw new IllegalArgumentException("Invalid UUID string: " + uuid);
        }
    }

    public static String randomUnsignedUUID() {
        return unsign(UUID.randomUUID());
    }

    public static String randomUnsignedUuidBase64() {
        return Base64.getEncoder().encodeToString(randomUnsignedUUID().toUpperCase().getBytes());
    }

}
