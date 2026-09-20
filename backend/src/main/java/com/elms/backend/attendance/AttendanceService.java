package com.elms.backend.attendance;

import com.elms.backend.attendance.dto.AttendanceResponse;
import com.elms.backend.attendance.dto.AttendanceSummaryResponse;
import com.elms.backend.attendance.dto.CheckInRequest;
import com.elms.backend.attendance.dto.TodayAttendanceResponse;
import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    public static final LocalTime WORK_START_TIME = LocalTime.of(9, 0, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @org.springframework.beans.factory.annotation.Value("${elms.attendance.work-start:09:00}")
    private String workStartConfig = "09:00";

    public LocalTime getWorkStartTime() {
        try {
            if (workStartConfig != null && !workStartConfig.isBlank()) {
                return LocalTime.parse(workStartConfig.trim());
            }
        } catch (Exception ignored) {
        }
        return WORK_START_TIME;
    }

    @Transactional
    public AttendanceResponse checkIn(UUID employeeId, CheckInRequest request) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new IllegalStateException("Only active employees can record attendance");
        }

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existingOpt.isPresent() && existingOpt.get().getCheckIn() != null) {
            String timeStr = existingOpt.get().getCheckIn().toLocalTime().format(TIME_FORMATTER);
            throw new DuplicateResourceException("Anda sudah melakukan check-in hari ini pada pukul " + timeStr);
        }

        AttendanceStatus status = now.toLocalTime().isAfter(getWorkStartTime())
                ? AttendanceStatus.LATE
                : AttendanceStatus.ON_TIME;

        Attendance attendance = existingOpt.orElseGet(() -> Attendance.builder()
                .employee(employee)
                .date(today)
                .build());

        attendance.setCheckIn(now);
        attendance.setStatus(status);
        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            attendance.setNotes(request.getNotes().trim());
        }

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(UUID employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException("Anda belum melakukan check-in hari ini"));

        if (attendance.getCheckIn() == null) {
            throw new ResourceNotFoundException("Anda belum melakukan check-in hari ini");
        }

        if (attendance.getCheckOut() != null) {
            String timeStr = attendance.getCheckOut().toLocalTime().format(TIME_FORMATTER);
            throw new DuplicateResourceException("Anda sudah melakukan check-out hari ini pada pukul " + timeStr);
        }

        attendance.setCheckOut(now);
        long minutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
        attendance.setWorkMinutes((int) minutes);

        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TodayAttendanceResponse getTodayStatus(UUID employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .map(TodayAttendanceResponse::fromEntity)
                .orElseGet(() -> TodayAttendanceResponse.notCheckedIn(today));
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyAttendanceHistory(UUID employeeId, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Attendance> spec = Specification.allOf(
                AttendanceSpecification.withEmployeeId(employeeId),
                AttendanceSpecification.withDateRange(from, to)
        );

        return attendanceRepository.findAll(spec, pageable)
                .map(AttendanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getTeamAttendance(UUID managerId, UUID employeeId, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Attendance> spec = Specification.allOf(
                AttendanceSpecification.withManagerId(managerId),
                AttendanceSpecification.withEmployeeId(employeeId),
                AttendanceSpecification.withDateRange(from, to)
        );

        return attendanceRepository.findAll(spec, pageable)
                .map(AttendanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAllAttendance(Long departmentId, UUID employeeId, AttendanceStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Attendance> spec = Specification.allOf(
                AttendanceSpecification.withDepartmentId(departmentId),
                AttendanceSpecification.withEmployeeId(employeeId),
                AttendanceSpecification.withStatus(status),
                AttendanceSpecification.withDateRange(from, to)
        );

        return attendanceRepository.findAll(spec, pageable)
                .map(AttendanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getTodaySummary() {
        LocalDate today = LocalDate.now();
        long totalActive = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long present = attendanceRepository.countByDate(today);
        long onTime = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ON_TIME);
        long late = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE);
        long absent = Math.max(0, totalActive - present);

        return AttendanceSummaryResponse.builder()
                .date(today)
                .totalEmployees(totalActive)
                .present(present)
                .onTime(onTime)
                .late(late)
                .absent(absent)
                .build();
    }
}
