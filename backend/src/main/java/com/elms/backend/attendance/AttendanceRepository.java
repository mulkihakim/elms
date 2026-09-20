package com.elms.backend.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    boolean existsByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    List<Attendance> findByDate(LocalDate date);

    long countByDate(LocalDate date);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
}
