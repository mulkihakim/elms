package com.elms.backend.performance.dto;

import com.elms.backend.performance.ReviewPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPeriodResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, UPCOMING, COMPLETED
    private LocalDateTime createdAt;

    public static ReviewPeriodResponse fromEntity(ReviewPeriod entity) {
        if (entity == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        String status;
        if (today.isBefore(entity.getStartDate())) {
            status = "UPCOMING";
        } else if (today.isAfter(entity.getEndDate())) {
            status = "COMPLETED";
        } else {
            status = "ACTIVE";
        }

        return ReviewPeriodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(status)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
