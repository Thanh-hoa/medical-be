package com.example.medical_be.swagger;

public class AccountApiExamples {

    private AccountApiExamples() {}

    private static final String ACCOUNT_DATA = """
                {
                    "id": "1",
                    "name": "Nguyễn Văn A",
                    "birthday": "1990-01-15",
                    "phoneNumber": "0901234567",
                    "username": "nguyenvana@gmail.com",
                    "email": "nguyenvana@gmail.com",
                    "isActive": true,
                    "emailVerifyAt": "2026-01-15T10:30:00",
                    "photoUrl": null,
                    "createdAt": "2026-01-15T10:30:00",
                    "updatedAt": null
                }""";

    public static final String REGISTER_SUCCESS = """
            {
                "isError": false,
                "message": "Đăng ký tài khoản thành công",
                "data":""" + ACCOUNT_DATA + """
            }
            """;

    public static final String CREATE_ACCOUNT_SUCCESS = """
            {
                "isError": false,
                "message": "Tạo tài khoản thành công",
                "data":""" + ACCOUNT_DATA + """
            }
            """;

    public static final String INFO_ACCOUNT_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy thông tin tài khoản thành công",
                "data":""" + ACCOUNT_DATA + """
            }
            """;

    public static final String UPDATE_SUCCESS = """
            {
                "isError": false,
                "message": "Cập nhật tài khoản thành công",
                "data":""" + ACCOUNT_DATA + """
            }
            """;

    public static final String VALIDATE_TOKEN_SUCCESS = """
            {
                "isError": false,
                "message": "Kích hoạt tài khoản thành công"
            }
            """;

    public static final String DELETE_SUCCESS = """
            {
                "isError": false,
                "message": "Xóa tài khoản thành công"
            }
            """;

    public static final String LIST_ACCOUNT_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy danh sách tài khoản thành công",
                "data": {
                    "items": [
                        {
                            "id": "1",
                            "name": "Nguyễn Văn A",
                            "birthday": "1990-01-15",
                            "phoneNumber": "0901234567",
                            "username": "nguyenvana@gmail.com",
                            "email": "nguyenvana@gmail.com",
                            "isActive": true,
                            "emailVerifyAt": "2026-01-15T10:30:00",
                            "photoUrl": null,
                            "createdAt": "2026-01-15T10:30:00",
                            "updatedAt": null
                        },
                        {
                            "id": "2",
                            "name": "Trần Thị B",
                            "birthday": "1995-05-20",
                            "phoneNumber": "0912345678",
                            "username": "tranthib@gmail.com",
                            "email": "tranthib@gmail.com",
                            "isActive": true,
                            "emailVerifyAt": "2026-02-10T08:00:00",
                            "photoUrl": null,
                            "createdAt": "2026-02-10T08:00:00",
                            "updatedAt": null
                        }
                    ],
                    "currentPage": 1,
                    "limit": 20,
                    "totalItems": 2,
                    "totalPage": 1
                }
            }
            """;

    public static final String EMAIL_EXISTS_ERROR = """
            {
                "isError": true,
                "message": "Email đã tồn tại"
            }
            """;

    public static final String NOT_FOUND_ERROR = """
            {
                "isError": true,
                "message": "Tài khoản không tồn tại"
            }
            """;
}
