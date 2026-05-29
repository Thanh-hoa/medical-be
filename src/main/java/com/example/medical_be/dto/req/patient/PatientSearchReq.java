package com.example.medical_be.dto.req.patient;

import com.example.medical_be.support.constant.CommonConstant;

public record PatientSearchReq(
        String q,       
        Integer page,
        Integer limit
) {
        public PatientSearchReq {
                page = (page != null) ? page : CommonConstant.DEFAULT_PAGE;
                limit = (limit != null) ? limit : CommonConstant.DEFAULT_LIMIT;
        }
}
