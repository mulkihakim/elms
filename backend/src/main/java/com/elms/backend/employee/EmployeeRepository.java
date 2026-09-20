package com.elms.backend.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByPositionId(Long positionId);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByEmploymentStatus(EmploymentStatus status);

    long countByEmploymentStatus(EmploymentStatus status);
}
