package com.example.medical_be.dto.res;

import java.time.LocalDate;
import java.util.List;

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
public class DashboardTimelineRes {
    String metric;
    String period;
    LocalDate fromDate;
    LocalDate toDate;
    List<DashboardStatItemRes> items;
}
