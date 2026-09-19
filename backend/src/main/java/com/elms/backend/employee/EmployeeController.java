package com.elms.backend.employee;

import com.elms.backend.common.response.ApiResponse;
import com.elms.backend.employee.dto.EmployeeRequest;
import com.elms.backend.employee.dto.EmployeeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<EmployeeResponse> response = employeeService.getEmployees(departmentId, status, role, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getActiveEmployees() {
        List<EmployeeResponse> response = employeeService.getActiveEmployees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }
}
