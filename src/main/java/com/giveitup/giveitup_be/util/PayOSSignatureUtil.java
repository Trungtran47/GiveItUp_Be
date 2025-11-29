package com.giveitup.giveitup_be.util;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class PayOSSignatureUtil {

    public static boolean verifyWebhookSignature(Map<String, Object> data, String receivedSignature, String checksumKey) {
        try {
            // 1. Copy Map sang TreeMap để sort alphabet
            Map<String, Object> sorted = new TreeMap<>(data);

            // 2. Nối chuỗi key=value&key=value
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<String, Object>> it = sorted.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                Object value = entry.getValue();
                if (value == null) value = ""; // null -> empty string
                sb.append(entry.getKey()).append("=").append(value);
                if (it.hasNext()) sb.append("&");
            }

            String dataString = sb.toString();

            // 3. HMAC-SHA256
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hashBytes = sha256_HMAC.doFinal(dataString.getBytes(StandardCharsets.UTF_8));

            // 4. Convert bytes sang hex chữ thường
            StringBuilder hashHex = new StringBuilder();
            for (byte b : hashBytes) {
                hashHex.append(String.format("%02x", b));
            }

            return hashHex.toString().equals(receivedSignature);

        } catch (Exception e) {
            throw new RuntimeException("Error verifying PayOS signature", e);
        }
    }


}
