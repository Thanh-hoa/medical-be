package com.example.medical_be.swagger;

public class MedicineApiExamples {

    private MedicineApiExamples() {}

    public static final String MEDICINE_LIST_SUCCESS = """
            {
                "isError": false,
                "message": "Lấy danh sách thuốc thành công",
                "data": {
                    "items": [
                        {
                            "id": 1,
                            "code": "MED-0001",
                            "name": "Paracetamol",
                            "strength": "500mg",
                            "unit": "viên",
                            "dosageForm": "viên nén",
                            "description": "Hạ sốt, giảm đau"
                        },
                        {
                            "id": 2,
                            "code": "MED-0002",
                            "name": "Amoxicillin",
                            "strength": "500mg",
                            "unit": "viên",
                            "dosageForm": "viên nang",
                            "description": "Kháng sinh nhóm penicillin"
                        }
                    ],
                    "currentPage": 1,
                    "limit": 20,
                    "totalItems": 2,
                    "totalPage": 1
                }
            }
            """;
}
