package com.example.medical_be.dto.res;

import java.util.List;

public record MenuItemRes(
    Long id,
    String key, 
    String label,
    String path,
    List<String> actions
    ) {}
