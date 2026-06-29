package com.example.medical_be.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HashUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private HashUtil() {
    }

    public static String hmacSha256(String value, String secretKey) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(
                    secretKey != null ? secretKey.getBytes(StandardCharsets.UTF_8) : new byte[0],
                    ALGORITHM));
            byte[] hash = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash value", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
