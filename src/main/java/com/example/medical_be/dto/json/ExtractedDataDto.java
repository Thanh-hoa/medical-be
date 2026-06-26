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

    // Thông tin cơ sở / phiếu
    String facility;
    String department;
    String signerName;
    

    // Chẩn đoán
    String diagnosis;

    // Trường OCR chưa phân loại hoặc dữ liệu cũ dạng flat map
    Map<String, String> extra;

    @JsonAnySetter
    public void addToExtra(String key, String value) {
        if (extra == null) extra = new HashMap<>();
        extra.put(key, value);
    }
}
