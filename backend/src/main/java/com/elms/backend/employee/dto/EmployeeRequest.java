package com.elms.backend.employee.dto;

import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Position is required")
    private Long positionId;

    private UUID managerId;

    @NotNull(message = "Join date is required")
    private LocalDate joinDate;

    private EmploymentStatus employmentStatus;

    private Role role;

    @Min(value = 0, message = "Leave balance cannot be negative")
    private Integer leaveBalance;

    private String password;
}
