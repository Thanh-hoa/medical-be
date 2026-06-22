package com.example.medical_be.dto.res;

import java.time.LocalDate;

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
public class DashboardRangeRes {
    String period;
    LocalDate fromDate;
    LocalDate toDate;
    LocalDate previousFromDate;
    LocalDate previousToDate;
}
