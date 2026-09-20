package com.elms.backend.performance.dto;

import com.elms.backend.performance.PerformanceReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewResponse {

    private UUID id;
    private Long reviewPeriodId;
    private String reviewPeriodName;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private String positionTitle;
    private UUID reviewerId;
    private String reviewerName;
    private Integer technicalSkill;
    private Integer communication;
    private Integer teamwork;
    private Integer problemSolving;
    private BigDecimal overallScore;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PerformanceReviewResponse fromEntity(PerformanceReview entity) {
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

        return PerformanceReviewResponse.builder()
                .id(entity.getId())
                .reviewPeriodId(entity.getReviewPeriod() != null ? entity.getReviewPeriod().getId() : null)
                .reviewPeriodName(entity.getReviewPeriod() != null ? entity.getReviewPeriod().getName() : null)
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .employeeEmail(entity.getEmployee() != null ? entity.getEmployee().getEmail() : null)
                .departmentName(deptName)
                .positionTitle(posTitle)
                .reviewerId(entity.getReviewer() != null ? entity.getReviewer().getId() : null)
                .reviewerName(entity.getReviewer() != null ? entity.getReviewer().getFullName() : null)
                .technicalSkill(entity.getTechnicalSkill())
                .communication(entity.getCommunication())
                .teamwork(entity.getTeamwork())
                .problemSolving(entity.getProblemSolving())
                .overallScore(entity.getOverallScore())
                .comments(entity.getComments())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
