package com.elms.backend.leave.dto;

import com.elms.backend.leave.LeaveRequest;
import com.elms.backend.leave.LeaveStatus;
import com.elms.backend.leave.LeaveType;
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
public class LeaveRequestResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private String positionTitle;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer requestedDays;
    private String reason;
    private LeaveStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;

    public static LeaveRequestResponse fromEntity(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }

        String deptName = null;
        String posTitle = null;
        if (entity.getEmployee() != null) {
            if (entity.getEmployee().getDepartment() != null) {
                deptName = entity.getEmployee().getDepartment().getName();
            }
            if (entity.getEmployee().getPosition() != null) {
                posTitle = entity.getEmployee().getPosition().getTitle();
            }
        }

        UUID approverId = null;
        String approverName = null;
        if (entity.getApprovedBy() != null) {
            approverId = entity.getApprovedBy().getId();
            approverName = entity.getApprovedBy().getFullName();
        }

        return LeaveRequestResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .employeeEmail(entity.getEmployee() != null ? entity.getEmployee().getEmail() : null)
                .departmentName(deptName)
                .positionTitle(posTitle)
                .leaveType(entity.getLeaveType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .requestedDays(entity.getRequestedDays())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .approvedById(approverId)
                .approvedByName(approverName)
                .decidedAt(entity.getDecidedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
