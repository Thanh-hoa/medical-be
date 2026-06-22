package com.example.medical_be.routes;

public class APIRoutes {
    public static final String API_V1 = "/api/v1";

    public static final String LOGIN =   "auth/login";
    public static final String LOGOUT =   "auth/logout";
    public static final String REFRESH_TOKEN =   "auth/refresh-token";
    public static final String FORGOT_PASSWORD = "auth/forgot-password";
    public static final String RESET_PASSWORD =  "auth/reset-password";

    public static final String REGISTER =   "account/register";
    public static final String CREATE_ACCOUNT =   "account/create";
    public static final String VALIDATE_TOKEN =  "account/validate-token";
    public static final String PROFILE =  "account/profile";
    public static final String CHANGE_PASSWORD = "account/change-password";
    public static final String DETAIL_ACCOUNT = "account/{id}";
    public static final String UPDATE_ACCOUNT = "account/update";
    public static final String LIST_ACCOUNT = "account/list";
    public static final String DELETE_ACCOUNT = "account/delete";
    public static final String MY_PERMISSIONS = "account/my-permissions";
    public static final String CONFIG_PERMISSION_MENU = "config/permission/menu";

    public static final String UPLOAD_MEDIA = "common/upload/media";
    public static final String GET_ROLES    = "common/roles";

    // Patient
    public static final String PATIENT_SEARCH      = "patient/search";   // GET ?bhyt=... → MedicalRecordSummaryPatient
    public static final String PATIENT_LIST         = "patient/list";     // GET ?q=&page=&limit= → PagedResponse
    public static final String PATIENT_DETAIL       = "patient/{id}";     // GET → PatientRes
    public static final String PATIENT_CREATE       = "patient/create";
    public static final String PATIENT_UPDATE       = "patient/update/{id}";

    // Medical Record
    public static final String MEDICAL_RECORD_CREATE        = "medical-record/create";
    public static final String MEDICAL_RECORD_UPLOAD        = "medical-record/upload";
    public static final String MEDICAL_RECORD_LIST          = "medical-record/list";
    public static final String MEDICAL_RECORD_DETAIL        = "medical-record/{id}";
    public static final String MEDICAL_RECORD_UPDATE        = "medical-record/update";
    public static final String MEDICAL_RECORD_UPDATE_DETAIL = "medical-record/update-detail";
    public static final String MEDICAL_RECORD_PENDING_REVIEW = "medical-record/pending-review";
    public static final String MEDICAL_RECORD_SUBMIT        = "medical-record/{id}/submit";
    public static final String MEDICAL_RECORD_APPROVE       = "medical-record/{id}/approve";
    public static final String MEDICAL_RECORD_REJECT        = "medical-record/{id}/reject";
    public static final String MEDICAL_RECORD_RESUBMIT      = "medical-record/{id}/resubmit";
    public static final String MEDICAL_RECORD_DELETE        = "medical-record/{id}";
    public static final String MEDICAL_RECORD_FIELD_UPDATE  = "medical-record/field/update";

    // Audit Log
    public static final String AUDIT_LOG_LIST               = "audit-logs";
    public static final String AUDIT_LOG_DETAIL             = "audit-logs/{id}";
    public static final String AUDIT_LOG_BY_RECORD          = "medical-record/{id}/audit-logs";

    // Dashboard
    public static final String DASHBOARD_OVERVIEW           = "dashboard/overview";
    public static final String DASHBOARD_RECORDS_BY_STATUS  = "dashboard/records-by-status";
    public static final String DASHBOARD_RECORDS_BY_DEPT    = "dashboard/records-by-department";
    public static final String DASHBOARD_USER_PERFORMANCE   = "dashboard/user-performance";
    public static final String DASHBOARD_TIMELINE           = "dashboard/timeline";

    // Notification
    public static final String NOTIFICATION_ROUTE           = "notifications";
    public static final String NOTIFICATION_LIST            = "notifications";
    public static final String NOTIFICATION_UNREAD_COUNT    = "notifications/unread-count";
    public static final String NOTIFICATION_MARK_READ       = "notifications/{id}/read";
    public static final String NOTIFICATION_MARK_ALL_READ   = "notifications/read-all";
    public static final String NOTIFICATION_DELETE          = "notifications/{id}";

    // Medicine
    public static final String MEDICINE_LIST               = "medicine/list";

    // Prescription
    public static final String PRESCRIPTION_CREATE_BY_RECORD = "prescription/medical-record/{recordId}";
    public static final String PRESCRIPTION_GET_BY_RECORD    = "prescription/medical-record/{recordId}";
    public static final String PRESCRIPTION_UPDATE           = "prescription/{id}";
    public static final String PRESCRIPTION_ISSUE            = "prescription/{id}/issue";
    public static final String PRESCRIPTION_PRINT            = "prescription/{id}/print";

    // Webhook (Admin only)
    public static final String WEBHOOK_ROUTE               = "admin/webhooks";
    public static final String WEBHOOK_LIST                = "admin/webhooks";
    public static final String WEBHOOK_CREATE              = "admin/webhooks";
    public static final String WEBHOOK_DETAIL              = "admin/webhooks/{id}";
    public static final String WEBHOOK_UPDATE              = "admin/webhooks/{id}";
    public static final String WEBHOOK_DELETE              = "admin/webhooks/{id}";
}
