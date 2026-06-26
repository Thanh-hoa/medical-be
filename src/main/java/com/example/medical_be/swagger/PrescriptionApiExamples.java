package com.example.medical_be.swagger;

public class PrescriptionApiExamples {

    private PrescriptionApiExamples() {}

    private static final String ITEM_DATA = """
                {
                    "id": 1,
                    "medicineId": 1,
                    "medicineName": "Paracetamol",
                    "strength": "500mg",
                    "unit": "viên",
                    "quantity": 20,
                    "morningDose": "1 viên",
                    "noonDose": null,
                    "afternoonDose": null,
                    "eveningDose": "1 viên",
                    "instruction": "Uống sau ăn",
                    "sortOrder": 0
                }""";

    private static final String PRESCRIPTION_DATA = """
                {
                    "id": 1,
                    "prescriptionNumber": "PRE-2026-000001",
                    "medicalRecordId": 10,
                    "patientId": 5,
                    "doctorId": 2,
                    "doctorName": "Trần Văn B",
                    "status": "DRAFT",
                    "hospitalName": "Bệnh viện Đa khoa ABC",
                    "receiverName": "Nguyễn Văn A",
                    "insuranceCode": "GD4030000123456",
                    "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
                    "diagnosis": "Viêm họng cấp",
                    "durationOption": null,
                    "durationDays": null,
                    "advice": null,
                    "issuedAt": null,
                    "createdAt": "2026-06-17T14:00:00",
                    "items": []
                }""";

    public static final String CREATE_SUCCESS = """
            {
                "isError": false,
                "message": "Tạo toa thuốc thành công",
                "data":""" + PRESCRIPTION_DATA + """
            }
            """;

    public static final String GET_BY_RECORD_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy toa thuốc thành công",
                "data":""" + PRESCRIPTION_DATA + """
            }
            """;

    public static final String GET_BY_RECORD_NULL = """
            {
                "isError": false,
                "message": "Lấy toa thuốc thành công",
                "data": null
            }
            """;

    public static final String UPDATE_SUCCESS = """
            {
                "isError": false,
                "message": "Cập nhật toa thuốc thành công",
                "data": {
                    "id": 1,
                    "prescriptionNumber": "PRE-2026-000001",
                    "medicalRecordId": 10,
                    "patientId": 5,
                    "doctorId": 2,
                    "doctorName": "Trần Văn B",
                    "status": "DRAFT",
                    "hospitalName": "Bệnh viện Đa khoa ABC",
                    "receiverName": "Nguyễn Văn A",
                    "insuranceCode": "GD4030000123456",
                    "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
                    "diagnosis": "Viêm họng cấp",
                    "durationOption": "TWO_WEEKS",
                    "durationDays": 14,
                    "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
                    "issuedAt": null,
                    "createdAt": "2026-06-17T14:00:00",
                    "items": [""" + ITEM_DATA + """
                    ]
                }
            }
            """;

    public static final String ISSUE_SUCCESS = """
            {
                "isError": false,
                "message": "Phát hành toa thuốc thành công",
                "data": {
                    "id": 1,
                    "prescriptionNumber": "PRE-2026-000001",
                    "medicalRecordId": 10,
                    "patientId": 5,
                    "doctorId": 2,
                    "doctorName": "Trần Văn B",
                    "status": "ISSUED",
                    "hospitalName": "Bệnh viện Đa khoa ABC",
                    "receiverName": "Nguyễn Văn A",
                    "insuranceCode": "GD4030000123456",
                    "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
                    "diagnosis": "Viêm họng cấp",
                    "durationOption": "TWO_WEEKS",
                    "durationDays": 14,
                    "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
                    "issuedAt": "2026-06-17T14:30:00",
                    "createdAt": "2026-06-17T14:00:00",
                    "items": [""" + ITEM_DATA + """
                    ]
                }
            }
            """;

    public static final String PRINT_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy dữ liệu in toa thành công",
                "data": {
                    "prescriptionNumber": "PRE-2026-000001",
                    "hospitalName": "Bệnh viện Đa khoa ABC",
                    "receiverName": "Nguyễn Văn A",
                    "insuranceCode": "GD4030000123456",
                    "receiverAddress": "123 Lê Lợi, Quận 1, TP.HCM",
                    "diagnosis": "Viêm họng cấp",
                    "doctorName": "Trần Văn B",
                    "durationDays": 14,
                    "advice": "Uống nhiều nước, tái khám nếu sốt trên 3 ngày",
                    "issuedAt": "2026-06-17T14:30:00",
                    "items": [
                        {
                            "id": 1,
                            "medicineId": 1,
                            "medicineName": "Paracetamol",
                            "strength": "500mg",
                            "unit": "viên",
                            "quantity": 20,
                            "morningDose": "1 viên",
                            "noonDose": null,
                            "afternoonDose": null,
                            "eveningDose": "1 viên",
                            "instruction": "Uống sau ăn",
                            "sortOrder": 0
                        }
                    ]
                }
            }
            """;
}
