package com.elms.backend.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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

    long countByManagerIdAndEmploymentStatus(UUID managerId, EmploymentStatus status);

    List<Employee> findByManagerIdAndEmploymentStatus(UUID managerId, EmploymentStatus status);

    @Query("SELECT COALESCE(d.name, 'Tanpa Departemen'), COUNT(e) FROM Employee e LEFT JOIN e.department d WHERE e.employmentStatus = com.elms.backend.employee.EmploymentStatus.ACTIVE GROUP BY d.name ORDER BY COUNT(e) DESC")
    List<Object[]> countActiveEmployeesByDepartment();

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdWithLock(@org.springframework.data.repository.query.Param("id") UUID id);
}
