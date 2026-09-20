package com.elms.backend.attendance;

import com.elms.backend.attendance.dto.AttendanceResponse;
import com.elms.backend.attendance.dto.AttendanceSummaryResponse;
import com.elms.backend.attendance.dto.CheckInRequest;
import com.elms.backend.attendance.dto.TodayAttendanceResponse;
import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    private UUID getEmployeeId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        return userDetails.getId();
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody(required = false) CheckInRequest request) {
        AttendanceResponse response = attendanceService.checkIn(getEmployeeId(userDetails), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Check-in successful"));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AttendanceResponse response = attendanceService.checkOut(getEmployeeId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response, "Check-out successful"));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayAttendanceResponse>> getTodayStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TodayAttendanceResponse response = attendanceService.getTodayStatus(getEmployeeId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getMyAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AttendanceResponse> response = attendanceService.getMyAttendanceHistory(getEmployeeId(userDetails), from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getTeamAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AttendanceResponse> response = attendanceService.getTeamAttendance(getEmployeeId(userDetails), employeeId, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAllAttendance(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AttendanceResponse> response = attendanceService.getAllAttendance(departmentId, employeeId, status, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getTodaySummary() {
        AttendanceSummaryResponse response = attendanceService.getTodaySummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
