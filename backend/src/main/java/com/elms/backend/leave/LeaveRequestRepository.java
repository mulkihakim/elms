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

    @Query("SELECT COUNT(DISTINCT l.employee.id) FROM LeaveRequest l " +
           "WHERE l.status = com.elms.backend.leave.LeaveStatus.APPROVED " +
           "AND :date BETWEEN l.startDate AND l.endDate")
    long countOnLeaveByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT l.employee.id) FROM LeaveRequest l " +
           "WHERE l.employee.manager.id = :managerId " +
           "AND l.status = com.elms.backend.leave.LeaveStatus.APPROVED " +
           "AND :date BETWEEN l.startDate AND l.endDate")
    long countTeamOnLeaveByDate(@Param("managerId") UUID managerId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(l) FROM LeaveRequest l " +
           "WHERE l.employee.manager.id = :managerId " +
           "AND l.status = com.elms.backend.leave.LeaveStatus.PENDING")
    long countTeamPendingLeaveRequests(@Param("managerId") UUID managerId);

    java.util.Optional<LeaveRequest> findFirstByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}
