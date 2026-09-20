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
import com.elms.backend.employee.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        employee = Employee.builder()
                .id(employeeId)
                .fullName("Ahmad Fauzi")
                .email("ahmad.emp@elms.com")
                .role(Role.EMPLOYEE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    @Test
    void checkIn_Success() {
        LocalDate today = LocalDate.now();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        CheckInRequest request = new CheckInRequest("Di kantor pusat");
        AttendanceResponse response = attendanceService.checkIn(employeeId, request);

        assertNotNull(response);
        assertEquals(employeeId, response.getEmployeeId());
        assertNotNull(response.getCheckIn());
        assertEquals("Di kantor pusat", response.getNotes());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void checkIn_AlreadyCheckedIn_ThrowsDuplicateException() {
        LocalDate today = LocalDate.now();
        Attendance existing = Attendance.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .date(today)
                .checkIn(today.atTime(8, 30))
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> attendanceService.checkIn(employeeId, null));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkOut_Success() {
        LocalDate today = LocalDate.now();
        LocalDateTime checkInTime = LocalDateTime.now().minusHours(8);
        Attendance existing = Attendance.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .date(today)
                .checkIn(checkInTime)
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(employeeId);

        assertNotNull(response);
        assertNotNull(response.getCheckOut());
        assertTrue(response.getWorkMinutes() >= 479); // ~480 minutes (8 hours)
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void checkOut_WithoutCheckIn_ThrowsNotFoundException() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.checkOut(employeeId));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void checkOut_AlreadyCheckedOut_ThrowsDuplicateException() {
        LocalDate today = LocalDate.now();
        Attendance existing = Attendance.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .date(today)
                .checkIn(today.atTime(8, 30))
                .checkOut(today.atTime(17, 0))
                .status(AttendanceStatus.ON_TIME)
                .workMinutes(510)
                .build();

        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> attendanceService.checkOut(employeeId));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void getTodayStatus_NotCheckedIn() {
        LocalDate today = LocalDate.now();
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, today)).thenReturn(Optional.empty());

        TodayAttendanceResponse response = attendanceService.getTodayStatus(employeeId);

        assertNotNull(response);
        assertFalse(response.isCheckedIn());
        assertFalse(response.isCheckedOut());
    }

    @Test
    void getTodaySummary() {
        LocalDate today = LocalDate.now();
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE)).thenReturn(10L);
        when(attendanceRepository.countByDate(today)).thenReturn(8L);
        when(attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ON_TIME)).thenReturn(6L);
        when(attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE)).thenReturn(2L);

        AttendanceSummaryResponse summary = attendanceService.getTodaySummary();

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalEmployees());
        assertEquals(8L, summary.getPresent());
        assertEquals(6L, summary.getOnTime());
        assertEquals(2L, summary.getLate());
        assertEquals(2L, summary.getAbsent()); // 10 - 8 = 2
    }
}
