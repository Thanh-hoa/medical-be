package com.example.medical_be.routes;

public class APIRoutes {
    public static final String API_V1 = "/api/v1";

    public static final String LOGIN =   "auth/login";
    public static final String LOGOUT =   "auth/logout";
    public static final String REFRESH_TOKEN =   "auth/refresh-token";

    public static final String REGISTER =   "account/register";
    public static final String CREATE_ACCOUNT =   "account/create";
    public static final String VALIDATE_TOKEN =  "account/validate-token";
    public static final String PROFILE =  "account/profile";
    public static final String DETAIL_ACCOUNT = "account/{id}";
    public static final String UPDATE_ACCOUNT = "account/update";
    public static final String LIST_ACCOUNT = "account/list";
    public static final String DELETE_ACCOUNT = "account/delete";

    public static final String UPLOAD_MEDIA = "common/upload/media";

    // Patient
    public static final String PATIENT_SEARCH      = "patient/search";   // GET ?bhyt=... → PatientWithRecordsRes
    public static final String PATIENT_LIST         = "patient/list";     // GET ?q=&page=&limit= → PagedResponse
    public static final String PATIENT_DETAIL       = "patient/{id}";     // GET → PatientRes
    public static final String PATIENT_CREATE       = "patient/create";
    public static final String PATIENT_UPDATE       = "patient/update/{id}";

    // Medical Record
    public static final String MEDICAL_RECORD_CREATE        = "medical-record/create";
    public static final String MEDICAL_RECORD_UPLOAD        = "medical-record/upload";
    public static final String MEDICAL_RECORD_OCR_RESULT    = "medical-record/ocr-result"; // Python AI gọi về
    public static final String MEDICAL_RECORD_LIST          = "medical-record/list";
    public static final String MEDICAL_RECORD_DETAIL        = "medical-record/{id}";
    public static final String MEDICAL_RECORD_UPDATE        = "medical-record/update";
    public static final String MEDICAL_RECORD_UPDATE_DETAIL = "medical-record/update-detail";
    public static final String MEDICAL_RECORD_SUBMIT        = "medical-record/{id}/submit";
    public static final String MEDICAL_RECORD_APPROVE       = "medical-record/{id}/approve";
    public static final String MEDICAL_RECORD_REJECT        = "medical-record/reject";
    public static final String MEDICAL_RECORD_DELETE        = "medical-record/{id}";
    public static final String MEDICAL_RECORD_FIELD_UPDATE  = "medical-record/field/update";
}
