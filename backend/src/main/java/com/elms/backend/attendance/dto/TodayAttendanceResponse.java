package com.elms.backend.attendance.dto;

import com.elms.backend.attendance.Attendance;
import com.elms.backend.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayAttendanceResponse {

    private LocalDate date;
    private boolean checkedIn;
    private boolean checkedOut;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status;
    private Integer workMinutes;
    private Long currentWorkMinutes;
    private String notes;

    public static TodayAttendanceResponse notCheckedIn(LocalDate date) {
        return TodayAttendanceResponse.builder()
                .date(date)
                .checkedIn(false)
                .checkedOut(false)
                .build();
    }

    public static TodayAttendanceResponse fromEntity(Attendance attendance) {
        Long currentMinutes = null;
        if (attendance.getCheckIn() != null) {
            if (attendance.getCheckOut() != null) {
                currentMinutes = (long) (attendance.getWorkMinutes() != null ? attendance.getWorkMinutes() : 0);
            } else {
                currentMinutes = Duration.between(attendance.getCheckIn(), LocalDateTime.now()).toMinutes();
            }
        }

        return TodayAttendanceResponse.builder()
                .date(attendance.getDate())
                .checkedIn(attendance.getCheckIn() != null)
                .checkedOut(attendance.getCheckOut() != null)
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .workMinutes(attendance.getWorkMinutes())
                .currentWorkMinutes(currentMinutes)
                .notes(attendance.getNotes())
                .build();
    }
}
