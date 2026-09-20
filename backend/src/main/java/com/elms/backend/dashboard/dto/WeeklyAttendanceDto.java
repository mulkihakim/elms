package com.elms.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAttendanceDto {
    private LocalDate date;
    private String dayName;
    private long presentCount;
    private long lateCount;
    private long onLeaveCount;
}
