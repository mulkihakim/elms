package com.elms.backend.performance;

import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.performance.dto.CreateReviewPeriodRequest;
import com.elms.backend.performance.dto.ReviewPeriodResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review-periods")
@RequiredArgsConstructor
public class ReviewPeriodController {

    private final PerformanceService performanceService;

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<ReviewPeriodResponse>> createPeriod(
            @Valid @RequestBody CreateReviewPeriodRequest request) {
        ReviewPeriodResponse response = performanceService.createPeriod(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Periode evaluasi berhasil dibuat"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewPeriodResponse>>> getAllPeriods() {
        List<ReviewPeriodResponse> response = performanceService.getAllPeriods();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewPeriodResponse>> getPeriodById(@PathVariable Long id) {
        ReviewPeriodResponse response = performanceService.getPeriodById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
