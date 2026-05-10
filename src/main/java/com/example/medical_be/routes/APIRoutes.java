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
}
