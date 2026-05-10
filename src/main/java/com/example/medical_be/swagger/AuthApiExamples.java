package com.example.medical_be.swagger;

public class AuthApiExamples {

    private AuthApiExamples() {}

    public static final String LOGIN_SUCCESS = """
            {
                "isError": false,
                "message": "Đăng nhập thành công",
                "data": {
                    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW52YW5hQGdtYWlsLmNvbSJ9.abc123",
                    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyZWZyZXNoIn0.xyz789",
                    "userId": 1,
                    "username": "nguyenvana@gmail.com"
                }
            }
            """;

    public static final String REFRESH_TOKEN_SUCCESS = """
            {
                "isError": false,
                "message": "Làm mới token thành công",
                "data": {
                    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW52YW5hQGdtYWlsLmNvbSJ9.newtoken",
                    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyZWZyZXNoIn0.newrefresh",
                    "userId": 1,
                    "username": "nguyenvana@gmail.com"
                }
            }
            """;

    public static final String LOGOUT_SUCCESS = """
            {
                "isError": false,
                "message": "Đăng xuất thành công",
                "data": {
                    "error": false,
                    "message": "Đăng xuất thành công"
                }
            }
            """;

    public static final String LOGIN_FAILED_ERROR = """
            {
                "isError": true,
                "message": "Đăng nhập thất bại, vui lòng kiểm tra lại thông tin và thời gian hết hạn của token"
            }
            """;

    public static final String ACCOUNT_INACTIVE_ERROR = """
            {
                "isError": true,
                "message": "Tài khoản chưa được kích hoạt, vui lòng kiểm tra email và kích hoạt tài khoản"
            }
            """;
}
