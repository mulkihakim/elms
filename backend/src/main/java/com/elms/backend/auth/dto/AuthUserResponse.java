package com.elms.backend.auth.dto;

import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private Role role;
    private String departmentName;
    private String positionTitle;
    private Integer leaveBalance;
    private EmploymentStatus employmentStatus;

    public static AuthUserResponse fromEntity(Employee employee) {
        return AuthUserResponse.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .role(employee.getRole())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionTitle(employee.getPosition() != null ? employee.getPosition().getTitle() : null)
                .leaveBalance(employee.getLeaveBalance())
                .employmentStatus(employee.getEmploymentStatus())
                .build();
    }
}
