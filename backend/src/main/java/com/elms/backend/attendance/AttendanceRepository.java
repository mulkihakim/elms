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

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.manager.id = :managerId AND a.date = :date")
    long countByManagerIdAndDate(
            @org.springframework.data.repository.query.Param("managerId") UUID managerId,
            @org.springframework.data.repository.query.Param("date") LocalDate date
    );

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.manager.id = :managerId AND a.date = :date AND a.status = :status")
    long countByManagerIdAndDateAndStatus(
            @org.springframework.data.repository.query.Param("managerId") UUID managerId,
            @org.springframework.data.repository.query.Param("date") LocalDate date,
            @org.springframework.data.repository.query.Param("status") AttendanceStatus status
    );

    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Attendance a WHERE a.employee.manager.id = :managerId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByManagerIdAndDateBetween(
            @org.springframework.data.repository.query.Param("managerId") UUID managerId,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
    );

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(UUID employeeId, LocalDate startDate, LocalDate endDate);
}
