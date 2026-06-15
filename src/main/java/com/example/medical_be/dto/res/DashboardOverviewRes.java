package com.example.medical_be.dto.res;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardOverviewRes {
    long totalRecords;
    long totalPatients;
    long totalAccounts;
    long processingRecords;
    long extractedRecords;
    long pendingReviewRecords;
    long approvedRecords;
    long rejectedRecords;
    long todayUploads;
    long todayApprovals;
}
