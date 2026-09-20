package com.elms.backend.dashboard;

import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.dashboard.dto.DashboardSummaryResponse;
import com.elms.backend.dashboard.dto.DepartmentDistributionDto;
import com.elms.backend.dashboard.dto.WeeklyAttendanceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DashboardSummaryResponse response = dashboardService.getSummary(userDetails);
        return ResponseEntity.ok(ApiResponse.success(response, "Data summary dashboard berhasil diambil"));
    }

    @GetMapping("/department-distribution")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<List<DepartmentDistributionDto>>> getDepartmentDistribution() {
        List<DepartmentDistributionDto> response = dashboardService.getDepartmentDistribution();
        return ResponseEntity.ok(ApiResponse.success(response, "Data distribusi departemen berhasil diambil"));
    }

    @GetMapping("/attendance-weekly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WeeklyAttendanceDto>>> getWeeklyAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WeeklyAttendanceDto> response = dashboardService.getWeeklyAttendance(userDetails);
        return ResponseEntity.ok(ApiResponse.success(response, "Data absensi mingguan berhasil diambil"));
    }
}
