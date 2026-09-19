package com.elms.backend.organization.position.dto;

import com.elms.backend.organization.position.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {

    private Long id;
    private String title;
    private Long departmentId;
    private String departmentName;

    public static PositionResponse fromEntity(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .title(position.getTitle())
                .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
                .departmentName(position.getDepartment() != null ? position.getDepartment().getName() : null)
                .build();
    }
}
