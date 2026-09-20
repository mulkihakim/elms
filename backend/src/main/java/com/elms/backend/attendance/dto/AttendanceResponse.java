package com.elms.backend.attendance.dto;

import com.elms.backend.attendance.Attendance;
import com.elms.backend.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private String positionTitle;
    private LocalDate date;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status;
    private Integer workMinutes;
    private String notes;
    private LocalDateTime createdAt;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFullName())
                .employeeEmail(attendance.getEmployee().getEmail())
                .departmentName(attendance.getEmployee().getDepartment() != null ? attendance.getEmployee().getDepartment().getName() : null)
                .positionTitle(attendance.getEmployee().getPosition() != null ? attendance.getEmployee().getPosition().getTitle() : null)
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .workMinutes(attendance.getWorkMinutes())
                .notes(attendance.getNotes())
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
