package com.example.medical_be.dto.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtractedDataDto {

   
    String facility;
    String department;
    String recordType;
    String signerName;
 
    String diagnosis;

    Map<String, String> extra;

    @JsonAnySetter
    public void addToExtra(String key, String value) {
        if (extra == null) extra = new HashMap<>();
        extra.put(key, value);
    }
}
