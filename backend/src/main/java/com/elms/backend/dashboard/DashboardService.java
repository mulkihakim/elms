package com.elms.backend.dashboard;

import com.elms.backend.attendance.Attendance;
import com.elms.backend.attendance.AttendanceRepository;
import com.elms.backend.attendance.AttendanceStatus;
import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.dashboard.dto.DashboardSummaryResponse;
import com.elms.backend.dashboard.dto.DepartmentDistributionDto;
import com.elms.backend.dashboard.dto.WeeklyAttendanceDto;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.LeaveRequest;
import com.elms.backend.leave.LeaveRequestRepository;
import com.elms.backend.leave.LeaveStatus;
import com.elms.backend.performance.PerformanceReview;
import com.elms.backend.performance.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    public DashboardSummaryResponse getSummary(CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        Role role = userDetails.getRole();
        UUID userId = userDetails.getId();

        DashboardSummaryResponse.DashboardSummaryResponseBuilder builder = DashboardSummaryResponse.builder();

        if (role == Role.HR) {
            builder.totalEmployees(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE));
            builder.presentToday(attendanceRepository.countByDate(today));
            builder.onLeaveToday(leaveRequestRepository.countOnLeaveByDate(today));
            builder.pendingLeaveRequests(leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        }

        if (role == Role.MANAGER) {
            builder.totalTeamMembers(employeeRepository.countByManagerIdAndEmploymentStatus(userId, EmploymentStatus.ACTIVE));
            builder.teamPresentToday(attendanceRepository.countByManagerIdAndDate(userId, today));
            builder.teamOnLeaveToday(leaveRequestRepository.countTeamOnLeaveByDate(userId, today));
            builder.teamPendingLeaveRequests(leaveRequestRepository.countTeamPendingLeaveRequests(userId));
        }

        // Employee personal metrics (for EMPLOYEE and can also be populated for MANAGER)
        if (role == Role.EMPLOYEE || role == Role.MANAGER) {
            Optional<Employee> employeeOpt = employeeRepository.findById(userId);
            Optional<Attendance> todayAttendanceOpt = attendanceRepository.findByEmployeeIdAndDate(userId, today);
            Optional<LeaveRequest> latestLeaveOpt = leaveRequestRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(userId);
            Optional<PerformanceReview> latestReviewOpt = performanceReviewRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(userId);

            builder.checkedInToday(todayAttendanceOpt.isPresent());
            todayAttendanceOpt.ifPresent(a -> {
                builder.checkInTime(a.getCheckIn() != null ? a.getCheckIn().toLocalTime() : null);
                builder.attendanceStatus(a.getStatus() != null ? a.getStatus().name() : null);
            });
            employeeOpt.ifPresent(e -> builder.remainingLeaveBalance(e.getLeaveBalance()));
            latestLeaveOpt.ifPresent(l -> builder.latestLeaveStatus(l.getStatus() != null ? l.getStatus().name() : null));
            latestReviewOpt.ifPresent(r -> {
                builder.latestReviewScore(r.getOverallScore() != null ? r.getOverallScore().doubleValue() : null);
                if (r.getReviewPeriod() != null) {
                    builder.latestReviewPeriodName(r.getReviewPeriod().getName());
                }
            });
        }

        return builder.build();
    }

    public List<DepartmentDistributionDto> getDepartmentDistribution() {
        List<Object[]> rows = employeeRepository.countActiveEmployeesByDepartment();
        List<DepartmentDistributionDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            String deptName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            list.add(new DepartmentDistributionDto(deptName, count));
        }
        return list;
    }

    public List<WeeklyAttendanceDto> getWeeklyAttendance(CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        Role role = userDetails.getRole();
        UUID userId = userDetails.getId();

        List<Attendance> attendances;
        if (role == Role.HR) {
            attendances = attendanceRepository.findByDateBetween(startDate, today);
        } else if (role == Role.MANAGER) {
            attendances = attendanceRepository.findByManagerIdAndDateBetween(userId, startDate, today);
        } else {
            attendances = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(userId, startDate, today);
        }

        Map<LocalDate, List<Attendance>> attendanceByDate = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getDate));

        List<WeeklyAttendanceDto> result = new ArrayList<>();
        Locale idLocale = Locale.of("id", "ID");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Attendance> dayAttendances = attendanceByDate.getOrDefault(date, Collections.emptyList());

            long presentCount = dayAttendances.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ON_TIME)
                    .count();

            long lateCount = dayAttendances.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long onLeaveCount;
            if (role == Role.HR) {
                onLeaveCount = leaveRequestRepository.countOnLeaveByDate(date);
            } else if (role == Role.MANAGER) {
                onLeaveCount = leaveRequestRepository.countTeamOnLeaveByDate(userId, date);
            } else {
                onLeaveCount = 0;
            }

            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, idLocale);

            result.add(WeeklyAttendanceDto.builder()
                    .date(date)
                    .dayName(dayName)
                    .presentCount(presentCount)
                    .lateCount(lateCount)
                    .onLeaveCount(onLeaveCount)
                    .build());
        }

        return result;
    }
}
