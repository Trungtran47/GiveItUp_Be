package com.giveitup.giveitup_be.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PayOSUtils {

    public static String generateSignature(Map<String, Object> data, String checksumKey) {
        try {
            // 1. Sort keys theo alphabet
            Map<String, Object> sorted = new TreeMap<>(data);

            // 2. Convert thành query string key=value&key=value
            String query = sorted.entrySet()
                    .stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));

            // 3. SHA256-HMAC
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(checksumKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKey);

            byte[] hash = sha256_HMAC.doFinal(query.getBytes());
            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error generating signature: " + e.getMessage());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
