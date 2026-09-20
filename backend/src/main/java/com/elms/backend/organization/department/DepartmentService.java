package com.elms.backend.organization.department;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ResourceInUseException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.organization.department.dto.DepartmentRequest;
import com.elms.backend.organization.department.dto.DepartmentResponse;
import com.elms.backend.organization.position.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .build();

        Department savedDepartment = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(savedDepartment);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(DepartmentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName());
        Department updatedDepartment = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        if (positionRepository.existsByDepartmentId(id)) {
            throw new ResourceInUseException("Cannot delete department because it still has associated positions");
        }
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new ResourceInUseException("Cannot delete department because it still has associated employees");
        }
        departmentRepository.deleteById(id);
    }
}
