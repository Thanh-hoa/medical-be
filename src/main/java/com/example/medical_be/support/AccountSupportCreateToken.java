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
     private final AppProperties appProperties;

     public  String generateTokenActiveAccount(String email){
            String signature = sign(email);
            String rawToken = email + ":" +signature;
         return Base64.getUrlEncoder()
                 .withoutPadding()
                 .encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
     }

     public String sign(String data){
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
}
