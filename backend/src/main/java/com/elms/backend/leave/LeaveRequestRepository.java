package com.elms.backend.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID>, JpaSpecificationExecutor<LeaveRequest> {

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l " +
           "WHERE l.employee.id = :employeeId " +
           "AND l.status IN :statuses " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    boolean existsOverlapping(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<LeaveStatus> statuses
    );

    long countByStatus(LeaveStatus status);
}
