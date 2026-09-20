package com.elms.backend.organization.position;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ResourceInUseException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.organization.department.Department;
import com.elms.backend.organization.department.DepartmentRepository;
import com.elms.backend.organization.position.dto.PositionRequest;
import com.elms.backend.organization.position.dto.PositionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (positionRepository.existsByTitleAndDepartmentId(request.getTitle(), request.getDepartmentId())) {
            throw new DuplicateResourceException("Position with title '" + request.getTitle() + "' already exists in this department");
        }

        Position position = Position.builder()
                .title(request.getTitle())
                .department(department)
                .build();

        Position savedPosition = positionRepository.save(position);
        return PositionResponse.fromEntity(savedPosition);
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> getAllPositions(Long departmentId, Pageable pageable) {
        if (departmentId != null) {
            return positionRepository.findByDepartmentId(departmentId, pageable)
                    .map(PositionResponse::fromEntity);
        }
        return positionRepository.findAll(pageable)
                .map(PositionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getPositionsByDepartment(Long departmentId) {
        return positionRepository.findByDepartmentId(departmentId).stream()
                .map(PositionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + id));
        return PositionResponse.fromEntity(position);
    }

    @Transactional
    public PositionResponse updatePosition(Long id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (positionRepository.existsByTitleAndDepartmentIdAndIdNot(request.getTitle(), request.getDepartmentId(), id)) {
            throw new DuplicateResourceException("Position with title '" + request.getTitle() + "' already exists in this department");
        }

        position.setTitle(request.getTitle());
        position.setDepartment(department);

        Position updatedPosition = positionRepository.save(position);
        return PositionResponse.fromEntity(updatedPosition);
    }

    @Transactional
    public void deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position not found with id: " + id);
        }
        if (employeeRepository.existsByPositionId(id)) {
            throw new ResourceInUseException("Cannot delete position because it still has associated employees");
        }
        positionRepository.deleteById(id);
    }
}
