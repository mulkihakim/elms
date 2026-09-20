package com.elms.backend.performance;

import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.employee.Role;
import com.elms.backend.performance.dto.CreatePerformanceReviewRequest;
import com.elms.backend.performance.dto.PerformanceReviewResponse;
import com.elms.backend.performance.dto.UpdatePerformanceReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceService performanceService;

    private UUID getEmployeeId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return userDetails.getId();
    }

    private Role getEmployeeRole(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return userDetails.getRole();
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePerformanceReviewRequest request) {
        PerformanceReviewResponse response = performanceService.createReview(getEmployeeId(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evaluasi kinerja berhasil disimpan"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePerformanceReviewRequest request) {
        PerformanceReviewResponse response = performanceService.updateReview(getEmployeeId(userDetails), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Evaluasi kinerja berhasil diperbarui"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<PerformanceReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PerformanceReviewResponse> response = performanceService.getMyReviews(getEmployeeId(userDetails), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<Page<PerformanceReviewResponse>>> getTeamReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PerformanceReviewResponse> response = performanceService.getTeamReviews(
                getEmployeeId(userDetails),
                getEmployeeRole(userDetails),
                periodId,
                employeeId,
                departmentId,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
