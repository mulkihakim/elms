package com.elms.backend.employee.dto;

import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
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
public class EmployeeResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;

    private Long departmentId;
    private String departmentName;

    private Long positionId;
    private String positionTitle;

    private UUID managerId;
    private String managerName;

    private LocalDate joinDate;
    private EmploymentStatus employmentStatus;
    private Role role;
    private Integer leaveBalance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse fromEntity(Employee employee) {
        if (employee == null) return null;

        return EmployeeResponse.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
                .positionTitle(employee.getPosition() != null ? employee.getPosition().getTitle() : null)
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFullName() : null)
                .joinDate(employee.getJoinDate())
                .employmentStatus(employee.getEmploymentStatus())
                .role(employee.getRole())
                .leaveBalance(employee.getLeaveBalance())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
