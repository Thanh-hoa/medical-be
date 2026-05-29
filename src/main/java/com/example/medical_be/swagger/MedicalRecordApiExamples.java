package com.example.medical_be.swagger;

public class MedicalRecordApiExamples {

    private MedicalRecordApiExamples() {}

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

    private static final String SUMMARY_DATA = """
                {
                    "id": 1,
                    "recordNumber": "REC-2026-000001",
                    "status": "Extracted",
                    "department": "Xét nghiệm",
                    "recordType": "Kết quả xét nghiệm máu",
                    "fileName": "xetnghiem_20260510.jpg",
                    "fileType": "image/jpeg",
                    "uploadedBy": 3,
                    "patient":""" + PATIENT_DATA + """
                ,
                    "createdAt": "2026-05-10T14:30:00",
                    "updatedAt": null
                }""";

    public static final String UPLOAD_SUCCESS = """
            {
                "isError": false,
                "message": "Upload và xử lý OCR thành công",
                "data": {
                    "id": 1,
                    "recordNumber": "REC-2026-000001",
                    "status": "Extracted",
                    "department": "Xét nghiệm",
                    "recordType": "Kết quả xét nghiệm máu",
                    "fileName": "xetnghiem_20260510.jpg",
                    "fileType": "image/jpeg",
                    "originalImagePath": "/uploads/records/xetnghiem_20260510.jpg",
                    "notes": null,
                    "rejectionReason": null,
                    "uploadedBy": 3,
                    "verifiedBy": null,
                    "verifiedAt": null,
                    "approvedBy": null,
                    "approvedAt": null,
                    "createdAt": "2026-05-10T14:30:00",
                    "updatedAt": null,
                    "patient":""" + PATIENT_DATA + """
                ,
                    "extractedData": {
                        "patientName": "Nguyễn Văn A",
                        "bhyt": "GD4030000123456",
                        "dob": "1990-05-15",
                        "gender": "Nam",
                        "department": "Xét nghiệm"
                    },
                    "labData": [
                        {
                            "testName": "Glucose",
                            "testValue": "5.2",
                            "unit": "mmol/L",
                            "referenceRange": "3.9 - 6.1",
                            "isAbnormal": false
                        },
                        {
                            "testName": "HbA1c",
                            "testValue": "7.8",
                            "unit": "%",
                            "referenceRange": "< 6.5",
                            "isAbnormal": true
                        }
                    ]
                }
            }
            """;

    public static final String OCR_RESULT_SUCCESS = """
            {
                "isError": false,
                "message": "Lưu kết quả OCR thành công",
                "data":""" + SUMMARY_DATA + """
            }
            """;

    public static final String LIST_RECORD_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy danh sách bệnh án thành công",
                "data": {
                    "items": [
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
                        },
                        {
                            "id": 2,
                            "recordNumber": "REC-2026-000002",
                            "status": "Pending Doctor Review",
                            "department": "Chẩn đoán hình ảnh",
                            "recordType": "Kết quả siêu âm",
                            "fileName": "sieuu_20260512.jpg",
                            "fileType": "image/jpeg",
                            "uploadedBy": 3,
                            "patient": {
                                "id": 2,
                                "bhyt": "GD4030000654321",
                                "name": "Trần Thị B",
                                "dob": "1985-08-20",
                                "gender": "Nữ",
                                "address": "456 Nguyễn Huệ, Quận 1, TP.HCM",
                                "phone": "0912345678",
                                "createdAt": "2026-02-01T08:00:00",
                                "updatedAt": null
                            },
                            "createdAt": "2026-05-12T10:00:00",
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

    public static final String DETAIL_RECORD_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy chi tiết bệnh án thành công",
                "data": {
                    "id": 1,
                    "recordNumber": "REC-2026-000001",
                    "status": "Approved",
                    "department": "Xét nghiệm",
                    "recordType": "Kết quả xét nghiệm máu",
                    "fileName": "xetnghiem_20260510.jpg",
                    "fileType": "image/jpeg",
                    "originalImagePath": "/uploads/records/xetnghiem_20260510.jpg",
                    "notes": "Bệnh nhân cần tái khám sau 1 tháng",
                    "rejectionReason": null,
                    "uploadedBy": 3,
                    "verifiedBy": 3,
                    "verifiedAt": "2026-05-10T15:00:00",
                    "approvedBy": 2,
                    "approvedAt": "2026-05-11T09:00:00",
                    "createdAt": "2026-05-10T14:30:00",
                    "updatedAt": "2026-05-11T09:00:00",
                    "patient":""" + PATIENT_DATA + """
                ,
                    "extractedData": {
                        "patientName": "Nguyễn Văn A",
                        "bhyt": "GD4030000123456",
                        "dob": "1990-05-15",
                        "gender": "Nam",
                        "department": "Xét nghiệm",
                        "testDate": "2026-05-10"
                    },
                    "labData": [
                        {
                            "testName": "Glucose",
                            "testValue": "5.2",
                            "unit": "mmol/L",
                            "referenceRange": "3.9 - 6.1",
                            "isAbnormal": false
                        },
                        {
                            "testName": "HbA1c",
                            "testValue": "7.8",
                            "unit": "%",
                            "referenceRange": "< 6.5",
                            "isAbnormal": true
                        },
                        {
                            "testName": "Cholesterol toàn phần",
                            "testValue": "4.8",
                            "unit": "mmol/L",
                            "referenceRange": "< 5.2",
                            "isAbnormal": false
                        }
                    ]
                }
            }
            """;

    public static final String UPDATE_DETAIL_SUCCESS = """
            {
                "isError": false,
                "message": "Cập nhật chi tiết bệnh án thành công",
                "data": {
                    "id": 1,
                    "recordNumber": "REC-2026-000001",
                    "status": "Extracted",
                    "department": "Xét nghiệm",
                    "recordType": "Kết quả xét nghiệm máu",
                    "fileName": "xetnghiem_20260510.jpg",
                    "fileType": "image/jpeg",
                    "originalImagePath": "/uploads/records/xetnghiem_20260510.jpg",
                    "notes": "Đã chỉnh sửa thủ công",
                    "rejectionReason": null,
                    "uploadedBy": 3,
                    "verifiedBy": null,
                    "verifiedAt": null,
                    "approvedBy": null,
                    "approvedAt": null,
                    "createdAt": "2026-05-10T14:30:00",
                    "updatedAt": "2026-05-10T16:00:00",
                    "patient":""" + PATIENT_DATA + """
                ,
                    "extractedData": {
                        "patientName": "Nguyễn Văn A",
                        "bhyt": "GD4030000123456",
                        "department": "Xét nghiệm"
                    },
                    "labData": []
                }
            }
            """;

    public static final String UPDATE_FIELD_SUCCESS = """
            {
                "isError": false,
                "message": "Cập nhật trường OCR thành công"
            }
            """;

    public static final String SUBMIT_SUCCESS = """
            {
                "isError": false,
                "message": "Gửi bệnh án để duyệt thành công",
                "data":""" + SUMMARY_DATA.replace("\"status\": \"Extracted\"", "\"status\": \"Pending Doctor Review\"") + """
            }
            """;

    public static final String APPROVE_SUCCESS = """
            {
                "isError": false,
                "message": "Duyệt bệnh án thành công",
                "data":""" + SUMMARY_DATA.replace("\"status\": \"Extracted\"", "\"status\": \"Approved\"") + """
            }
            """;

    public static final String REJECT_SUCCESS = """
            {
                "isError": false,
                "message": "Từ chối bệnh án thành công",
                "data":""" + SUMMARY_DATA.replace("\"status\": \"Extracted\"", "\"status\": \"Rejected\"") + """
            }
            """;

    public static final String DELETE_SUCCESS = """
            {
                "isError": false,
                "message": "Xóa bệnh án thành công"
            }
            """;
}
