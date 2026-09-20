package com.elms.backend.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {

    private LocalDate date;
    private long totalEmployees;
    private long present;
    private long onTime;
    private long late;
    private long absent;
}
