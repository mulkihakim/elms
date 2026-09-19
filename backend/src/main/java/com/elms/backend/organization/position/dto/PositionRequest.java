package com.elms.backend.organization.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionRequest {

    @NotBlank(message = "Position title is required")
    @Size(max = 100, message = "Position title cannot exceed 100 characters")
    private String title;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
