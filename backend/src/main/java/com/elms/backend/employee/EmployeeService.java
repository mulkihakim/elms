package com.elms.backend.employee;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.dto.EmployeeRequest;
import com.elms.backend.employee.dto.EmployeeResponse;
import com.elms.backend.organization.department.Department;
import com.elms.backend.organization.department.DepartmentRepository;
import com.elms.backend.organization.position.Position;
import com.elms.backend.organization.position.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + request.getPositionId()));

        if (!position.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Position must belong to the selected department");
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        Employee employee = Employee.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .department(department)
                .position(position)
                .manager(manager)
                .joinDate(request.getJoinDate())
                .employmentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE)
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .leaveBalance(request.getLeaveBalance() != null ? request.getLeaveBalance() : 12)
                .password(passwordEncoder.encode(request.getPassword() != null && !request.getPassword().isBlank() ? request.getPassword() : "password123"))
                .build();

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(Long departmentId, EmploymentStatus status, Role role, String search, Pageable pageable) {
        Specification<Employee> spec = Specification.allOf(
                EmployeeSpecification.withDepartment(departmentId),
                EmployeeSpecification.withEmploymentStatus(status),
                EmployeeSpecification.withRole(role),
                EmployeeSpecification.withSearch(search)
        );

        return employeeRepository.findAll(spec, pageable)
                .map(EmployeeResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE).stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getMyTeamMembers(UUID managerId) {
        return employeeRepository.findByManagerIdAndEmploymentStatus(managerId, EmploymentStatus.ACTIVE).stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (employeeRepository.existsByEmailAndIdNot(request.getEmail().trim().toLowerCase(), id)) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + request.getPositionId()));

        if (!position.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Position must belong to the selected department");
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new IllegalArgumentException("Employee cannot be their own manager");
            }
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        employee.setFullName(request.getFullName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setManager(manager);
        employee.setJoinDate(request.getJoinDate());

        if (request.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }
        if (request.getLeaveBalance() != null) {
            employee.setLeaveBalance(request.getLeaveBalance());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(updated);
    }

    @Transactional
    public EmployeeResponse updateEmployeeStatus(UUID id, EmploymentStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setEmploymentStatus(status);
        Employee updated = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(updated);
    }
}
