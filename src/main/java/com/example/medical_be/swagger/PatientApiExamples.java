package com.example.medical_be.swagger;

public class PatientApiExamples {

    private PatientApiExamples() {}

    private static final String PATIENT_DATA = """
                {
                    "id": 1,
                    "bhyt": "GD4030000123456",
                    "name": "Nguyễn Văn A",
                    "dob": "1990-05-15",
                    "gender": "Nam",
                    "address": "123 Lê Lợi, Quận 1, TP.HCM",
                    "phone": "0901234567",
                    "createdAt": "2026-01-10T09:00:00",
                    "updatedAt": null
                }""";

    public static final String SEARCH_BY_BHYT_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy thông tin bệnh nhân thành công",
                "data": {
                    "patient":""" + PATIENT_DATA + """
                ,
                    "records": [
                        {
                            "id": 2,
                            "recordNumber": "REC-2026-000002",
                            "status": "Approved",
                            "department": "Xét nghiệm",
                            "recordType": "Kết quả xét nghiệm máu",
                            "fileName": "xetnghiem_20260512.jpg",
                            "fileType": "image/jpeg",
                            "uploadedBy": 3,
                            "patient":""" + PATIENT_DATA + """
                        ,
                            "createdAt": "2026-05-12T10:00:00",
                            "updatedAt": "2026-05-13T09:00:00"
                        },
                        {
                            "id": 1,
                            "recordNumber": "REC-2026-000001",
                            "status": "Approved",
                            "department": "Xét nghiệm",
                            "recordType": "Kết quả xét nghiệm máu",
                            "fileName": "xetnghiem_20260510.jpg",
                            "fileType": "image/jpeg",
                            "uploadedBy": 3,
                            "patient":""" + PATIENT_DATA + """
                        ,
                            "createdAt": "2026-05-10T14:30:00",
                            "updatedAt": "2026-05-11T09:00:00"
                        }
                    ],
                    "totalRecords": 2
                }
            }
            """;

    public static final String LIST_PATIENT_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy danh sách bệnh nhân thành công",
                "data": {
                    "items": [
                        {
                            "id": 1,
                            "bhyt": "GD4030000123456",
                            "name": "Nguyễn Văn A",
                            "dob": "1990-05-15",
                            "gender": "Nam",
                            "address": "123 Lê Lợi, Quận 1, TP.HCM",
                            "phone": "0901234567",
                            "createdAt": "2026-01-10T09:00:00",
                            "updatedAt": null
                        },
                        {
                            "id": 2,
                            "bhyt": "GD4030000654321",
                            "name": "Trần Thị B",
                            "dob": "1985-08-20",
                            "gender": "Nữ",
                            "address": "456 Nguyễn Huệ, Quận 1, TP.HCM",
                            "phone": "0912345678",
                            "createdAt": "2026-02-01T08:00:00",
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

    public static final String CREATE_PATIENT_SUCCESS = """
            {
                "isError": false,
                "message": "Tạo bệnh nhân thành công",
                "data":""" + PATIENT_DATA + """
            }
            """;

    public static final String UPDATE_PATIENT_SUCCESS = """
            {
                "isError": false,
                "message": "Cập nhật thông tin bệnh nhân thành công",
                "data":""" + PATIENT_DATA + """
            }
            """;

    public static final String BHYT_NOT_FOUND_ERROR = """
            {
                "isError": true,
                "message": "Không tìm thấy bệnh nhân với số BHYT này"
            }
            """;

    public static final String BHYT_DUPLICATE_ERROR = """
            {
                "isError": true,
                "message": "Số thẻ BHYT đã tồn tại trong hệ thống"
            }
            """;
}
