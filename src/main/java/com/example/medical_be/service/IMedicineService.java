package com.example.medical_be.service;

import com.example.medical_be.dto.res.MedicineRes;
import com.example.medical_be.dto.res.PagedResponse;

public interface IMedicineService {
    PagedResponse<MedicineRes> search(String q, Integer page, Integer limit);
}
