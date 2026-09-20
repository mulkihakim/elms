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
import com.elms.backend.performance.ReviewPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID hrId;
    private UUID managerId;
    private UUID employeeId;

    private CustomUserDetails hrUser;
    private CustomUserDetails managerUser;
    private CustomUserDetails employeeUser;

    @BeforeEach
    void setUp() {
        hrId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        Employee hr = Employee.builder()
                .id(hrId)
                .email("hr@elms.com")
                .role(Role.HR)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        hrUser = new CustomUserDetails(hr);

        Employee manager = Employee.builder()
                .id(managerId)
                .email("mgr@elms.com")
                .role(Role.MANAGER)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .leaveBalance(12)
                .build();
        managerUser = new CustomUserDetails(manager);

        Employee employee = Employee.builder()
                .id(employeeId)
                .email("emp@elms.com")
                .role(Role.EMPLOYEE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .leaveBalance(10)
                .build();
        employeeUser = new CustomUserDetails(employee);
    }

    @Test
    @DisplayName("getSummary - HR: Mengembalikan metrik perusahaan (total karyawan, hadir, cuti, pending)")
    void getSummary_hrRole_returnsCompanyMetrics() {
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE)).thenReturn(25L);
        when(attendanceRepository.countByDate(any(LocalDate.class))).thenReturn(20L);
        when(leaveRequestRepository.countOnLeaveByDate(any(LocalDate.class))).thenReturn(3L);
        when(leaveRequestRepository.countByStatus(LeaveStatus.PENDING)).thenReturn(2L);

        DashboardSummaryResponse response = dashboardService.getSummary(hrUser);

        assertNotNull(response);
        assertEquals(25L, response.getTotalEmployees());
        assertEquals(20L, response.getPresentToday());
        assertEquals(3L, response.getOnLeaveToday());
        assertEquals(2L, response.getPendingLeaveRequests());
        assertNull(response.getTotalTeamMembers());
        assertNull(response.getCheckedInToday());
    }

    @Test
    @DisplayName("getSummary - Manager: Mengembalikan metrik tim dan metrik personal")
    void getSummary_managerRole_returnsTeamAndPersonalMetrics() {
        when(employeeRepository.countByManagerIdAndEmploymentStatus(eq(managerId), eq(EmploymentStatus.ACTIVE))).thenReturn(5L);
        when(attendanceRepository.countByManagerIdAndDate(eq(managerId), any(LocalDate.class))).thenReturn(4L);
        when(leaveRequestRepository.countTeamOnLeaveByDate(eq(managerId), any(LocalDate.class))).thenReturn(1L);
        when(leaveRequestRepository.countTeamPendingLeaveRequests(eq(managerId))).thenReturn(1L);

        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(Employee.builder().id(managerId).leaveBalance(12).build()));
        when(attendanceRepository.findByEmployeeIdAndDate(eq(managerId), any(LocalDate.class))).thenReturn(Optional.empty());
        when(leaveRequestRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(managerId)).thenReturn(Optional.empty());
        when(performanceReviewRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(managerId)).thenReturn(Optional.empty());

        DashboardSummaryResponse response = dashboardService.getSummary(managerUser);

        assertNotNull(response);
        assertNull(response.getTotalEmployees());
        assertEquals(5L, response.getTotalTeamMembers());
        assertEquals(4L, response.getTeamPresentToday());
        assertEquals(1L, response.getTeamOnLeaveToday());
        assertEquals(1L, response.getTeamPendingLeaveRequests());
        assertFalse(response.getCheckedInToday());
        assertEquals(12, response.getRemainingLeaveBalance());
    }

    @Test
    @DisplayName("getSummary - Employee: Mengembalikan metrik personal (kehadiran, saldo cuti, review)")
    void getSummary_employeeRole_returnsPersonalMetrics() {
        Attendance attendance = Attendance.builder()
                .id(UUID.randomUUID())
                .date(LocalDate.now())
                .checkIn(LocalDate.now().atTime(8, 45))
                .status(AttendanceStatus.ON_TIME)
                .build();

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(UUID.randomUUID())
                .status(LeaveStatus.APPROVED)
                .build();

        ReviewPeriod period = ReviewPeriod.builder()
                .id(1L)
                .name("Q1 2026")
                .build();

        PerformanceReview review = PerformanceReview.builder()
                .id(UUID.randomUUID())
                .reviewPeriod(period)
                .overallScore(new BigDecimal("4.50"))
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(Employee.builder().id(employeeId).leaveBalance(10).build()));
        when(attendanceRepository.findByEmployeeIdAndDate(eq(employeeId), any(LocalDate.class))).thenReturn(Optional.of(attendance));
        when(leaveRequestRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(employeeId)).thenReturn(Optional.of(leaveRequest));
        when(performanceReviewRepository.findFirstByEmployeeIdOrderByCreatedAtDesc(employeeId)).thenReturn(Optional.of(review));

        DashboardSummaryResponse response = dashboardService.getSummary(employeeUser);

        assertNotNull(response);
        assertNull(response.getTotalEmployees());
        assertNull(response.getTotalTeamMembers());
        assertTrue(response.getCheckedInToday());
        assertEquals(LocalTime.of(8, 45), response.getCheckInTime());
        assertEquals("ON_TIME", response.getAttendanceStatus());
        assertEquals(10, response.getRemainingLeaveBalance());
        assertEquals("APPROVED", response.getLatestLeaveStatus());
        assertEquals(4.50, response.getLatestReviewScore());
        assertEquals("Q1 2026", response.getLatestReviewPeriodName());
    }

    @Test
    @DisplayName("getDepartmentDistribution: Mengembalikan list distribusi karyawan")
    void getDepartmentDistribution_returnsList() {
        Object[] row1 = new Object[]{"Engineering", 15L};
        Object[] row2 = new Object[]{"Human Resources", 5L};
        List<Object[]> mockRows = List.of(row1, row2);

        when(employeeRepository.countActiveEmployeesByDepartment()).thenReturn(mockRows);

        List<DepartmentDistributionDto> result = dashboardService.getDepartmentDistribution();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Engineering", result.get(0).getDepartmentName());
        assertEquals(15L, result.get(0).getEmployeeCount());
    }

    @Test
    @DisplayName("getWeeklyAttendance - HR: Mengembalikan 7 hari kehadiran organisasi")
    void getWeeklyAttendance_hrRole_returnsWeeklyList() {
        when(attendanceRepository.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(leaveRequestRepository.countOnLeaveByDate(any(LocalDate.class))).thenReturn(0L);

        List<WeeklyAttendanceDto> result = dashboardService.getWeeklyAttendance(hrUser);

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals(LocalDate.now(), result.get(6).getDate());
        assertNotNull(result.get(6).getDayName());
    }
}
