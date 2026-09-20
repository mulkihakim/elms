package com.elms.backend.leave;

import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.dto.CreateLeaveRequest;
import com.elms.backend.leave.dto.LeaveRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

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
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> submitLeave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateLeaveRequest request) {
        LeaveRequestResponse response = leaveService.submit(getEmployeeId(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Pengajuan cuti berhasil dikirim"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getMyLeaves(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LeaveRequestResponse> response = leaveService.getMyLeaveRequests(getEmployeeId(userDetails), status, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getTeamLeaves(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LeaveRequestResponse> response = leaveService.getTeamLeaveRequests(
                getEmployeeId(userDetails),
                getEmployeeRole(userDetails),
                employeeId,
                departmentId,
                status,
                from,
                to,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        LeaveRequestResponse response = leaveService.approve(id, getEmployeeId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response, "Pengajuan cuti berhasil disetujui"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        LeaveRequestResponse response = leaveService.reject(id, getEmployeeId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response, "Pengajuan cuti telah ditolak"));
    }
}
