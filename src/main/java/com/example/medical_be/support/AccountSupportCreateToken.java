package com.example.medical_be.support;
import com.example.medical_be.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AccountSupportCreateToken {

    private static final long RESET_TOKEN_EXPIRY_MS = 15 * 60 * 1000L; // 15 minutes

    private final AppProperties appProperties;

    public String generateTokenActiveAccount(String email) {
        String signature = sign(email);
        String rawToken = email + ":" + signature;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    public String generatePasswordResetToken(String email) {
        long timestamp = System.currentTimeMillis();
        String payload = email + ":" + timestamp;
        String signature = sign(payload);
        String rawToken = payload + ":" + signature;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    // Returns email if valid, throws RuntimeException if invalid or expired
    public String validatePasswordResetToken(String token) {
        try {
            String rawToken = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = rawToken.split(":");
            if (parts.length != 3) return null;

            String email = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            String signature = parts[2];

            String payload = email + ":" + timestamp;
            String expectedSignature = sign(payload);
            if (!constantTimeEquals(signature, expectedSignature)) return null;

            if (System.currentTimeMillis() - timestamp > RESET_TOKEN_EXPIRY_MS) return null;

            return email;
        } catch (Exception e) {
            return null;
        }
    }

    public String sign(String data) {
         try {
             Mac mac = Mac.getInstance("HmacSHA256");
             SecretKeySpec secretKeySpec = new SecretKeySpec(appProperties.getSignKey() , "HmacSHA256");
             mac.init(secretKeySpec);
             byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
             return bytesToHex(hmacBytes);
         }catch (Exception e){
             throw  new RuntimeException( "Cannot sign token" , e );
         }
     }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
