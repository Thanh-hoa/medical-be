package com.example.medical_be.swagger;

public class ConfigurationApiExamples {

    public static final String MENU_RESPONSE = """
            {
              "isError": false,
              "data": [
                {
                  "key": "dashboard",
                  "label": "Dashboard",
                  "path": "/dashboard",
                  "actions": ["view"]
                },
                {
                  "key": "accounts",
                  "label": "Account Management",
                  "path": "/accounts",
                  "actions": ["view", "create", "edit", "delete"]
                },
                {
                  "key": "medical-records",
                  "label": "Medical Records",
                  "path": "/medical-records",
                  "actions": ["view", "create", "edit", "delete"]
                },
                {
                  "key": "medical-records-approval",
                  "label": "Medical Records Approval",
                  "path": "/medical-records-approval",
                  "actions": ["view", "create", "cancel"]
                },
                {
                  "key": "patient-search",
                  "label": "Patient Management",
                  "path": "/patient-search",
                  "actions": ["view", "create", "edit", "delete"]
                }
              ]
            }
            """;

    @Deprecated
    public static final String CONFIG_PERMISSION_MENU = MENU_RESPONSE;
}
