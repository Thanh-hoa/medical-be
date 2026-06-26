package com.example.medical_be.properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String key;

    public byte[] getSignKey(){
        return Base64.getDecoder().decode(key);
    }
}
