package com.elms.backend.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardSummaryResponse {

    // HR / Company scope
    private Long totalEmployees;
    private Long presentToday;
    private Long onLeaveToday;
    private Long pendingLeaveRequests;

    // Manager / Team scope
    private Long totalTeamMembers;
    private Long teamPresentToday;
    private Long teamOnLeaveToday;
    private Long teamPendingLeaveRequests;

    // Employee / Personal scope
    private Boolean checkedInToday;
    private LocalTime checkInTime;
    private String attendanceStatus;
    private Integer remainingLeaveBalance;
    private String latestLeaveStatus;
    private Double latestReviewScore;
    private String latestReviewPeriodName;
}
