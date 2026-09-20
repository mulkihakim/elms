package com.elms.backend.leave;

import com.elms.backend.common.exception.InsufficientLeaveBalanceException;
import com.elms.backend.common.exception.InvalidStatusTransitionException;
import com.elms.backend.common.exception.LeaveOverlapException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.dto.CreateLeaveRequest;
import com.elms.backend.leave.dto.LeaveRequestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private UUID employeeId;
    private UUID managerId;
    private UUID hrId;
    private UUID otherManagerId;

    private Employee manager;
    private Employee employee;
    private Employee hr;
    private Employee otherManager;

    @BeforeEach
    void setUp() {
        managerId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        hrId = UUID.randomUUID();
        otherManagerId = UUID.randomUUID();

        manager = Employee.builder()
                .id(managerId)
                .fullName("Budi Manager")
                .role(Role.MANAGER)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .leaveBalance(12)
                .build();

        employee = Employee.builder()
                .id(employeeId)
                .fullName("Siti Staff")
                .role(Role.EMPLOYEE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .manager(manager)
                .leaveBalance(10)
                .build();

        hr = Employee.builder()
                .id(hrId)
                .fullName("Dewi HR")
                .role(Role.HR)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .leaveBalance(12)
                .build();

        otherManager = Employee.builder()
                .id(otherManagerId)
                .fullName("Iwan Other Manager")
                .role(Role.MANAGER)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .leaveBalance(12)
                .build();
    }

    private LocalDate getNextDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate today = LocalDate.now(LeaveService.ZONE_JAKARTA);
        return today.with(TemporalAdjusters.next(dayOfWeek));
    }

    @Test
    @DisplayName("Submit: Berhasil mengajukan cuti dengan perhitungan hari kerja (skip weekend)")
    void submit_Success_WorkingDaysCalculatedExcludingWeekend() {
        // Ambil hari Senin dan Rabu di minggu depan
        LocalDate nextMonday = getNextDayOfWeek(DayOfWeek.MONDAY);
        LocalDate nextWednesday = nextMonday.plusDays(2); // Senin, Selasa, Rabu = 3 hari

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextMonday)
                .endDate(nextWednesday)
                .leaveType(LeaveType.ANNUAL)
                .reason("Liburan keluarga")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlapping(eq(employeeId), eq(nextMonday), eq(nextWednesday), any())).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            lr.setId(UUID.randomUUID());
            return lr;
        });

        LeaveRequestResponse response = leaveService.submit(employeeId, request);

        assertNotNull(response);
        assertEquals(3, response.getRequestedDays());
        assertEquals(LeaveStatus.PENDING, response.getStatus());
        assertEquals("Liburan keluarga", response.getReason());
        // Verifikasi saldo pemohon belum berkurang
        assertEquals(10, employee.getLeaveBalance());
    }

    @Test
    @DisplayName("Submit: Cuti melintasi weekend hanya menghitung hari kerja")
    void submit_Success_CrossingWeekend() {
        // Jumat s/d Senin depan = 2 hari kerja (Jumat & Senin)
        LocalDate nextFriday = getNextDayOfWeek(DayOfWeek.FRIDAY);
        LocalDate nextMonday = nextFriday.plusDays(3);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextFriday)
                .endDate(nextMonday)
                .leaveType(LeaveType.ANNUAL)
                .reason("Keperluan keluarga")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlapping(eq(employeeId), eq(nextFriday), eq(nextMonday), any())).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            lr.setId(UUID.randomUUID());
            return lr;
        });

        LeaveRequestResponse response = leaveService.submit(employeeId, request);

        assertNotNull(response);
        assertEquals(2, response.getRequestedDays());
    }

    @Test
    @DisplayName("Submit: Gagal jika startDate > endDate")
    void submit_ThrowsIllegalArgument_WhenStartDateAfterEndDate() {
        LocalDate nextMonday = getNextDayOfWeek(DayOfWeek.MONDAY);
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextMonday.plusDays(2))
                .endDate(nextMonday)
                .leaveType(LeaveType.ANNUAL)
                .reason("Invalid dates")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> leaveService.submit(employeeId, request));
    }

    @Test
    @DisplayName("Submit: Gagal jika tanggal di masa lampau (backdate)")
    void submit_ThrowsIllegalArgument_WhenBackdate() {
        LocalDate pastDate = LocalDate.now(LeaveService.ZONE_JAKARTA).minusDays(2);
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(pastDate)
                .endDate(pastDate.plusDays(1))
                .leaveType(LeaveType.ANNUAL)
                .reason("Backdate request")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> leaveService.submit(employeeId, request));
    }

    @Test
    @DisplayName("Submit: Gagal jika pengajuan hanya jatuh pada akhir pekan (0 hari kerja)")
    void submit_ThrowsIllegalArgument_WhenZeroWorkingDays() {
        LocalDate nextSaturday = getNextDayOfWeek(DayOfWeek.SATURDAY);
        LocalDate nextSunday = nextSaturday.plusDays(1);

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextSaturday)
                .endDate(nextSunday)
                .leaveType(LeaveType.ANNUAL)
                .reason("Weekend only")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> leaveService.submit(employeeId, request));
    }

    @Test
    @DisplayName("Submit: Gagal 409 jika overlap dengan pengajuan lain")
    void submit_ThrowsLeaveOverlap_WhenOverlappingRequestExists() {
        LocalDate nextMonday = getNextDayOfWeek(DayOfWeek.MONDAY);
        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextMonday)
                .endDate(nextMonday.plusDays(2))
                .leaveType(LeaveType.ANNUAL)
                .reason("Overlap test")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlapping(eq(employeeId), any(), any(), any())).thenReturn(true);

        assertThrows(LeaveOverlapException.class, () -> leaveService.submit(employeeId, request));
    }

    @Test
    @DisplayName("Submit: Gagal 409 jika sisa saldo cuti tidak cukup")
    void submit_ThrowsInsufficientBalance_WhenRequestedDaysExceedsLeaveBalance() {
        employee.setLeaveBalance(2);
        LocalDate nextMonday = getNextDayOfWeek(DayOfWeek.MONDAY);
        LocalDate nextThursday = nextMonday.plusDays(3); // 4 hari kerja

        CreateLeaveRequest request = CreateLeaveRequest.builder()
                .startDate(nextMonday)
                .endDate(nextThursday)
                .leaveType(LeaveType.ANNUAL)
                .reason("Too many days")
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlapping(eq(employeeId), any(), any(), any())).thenReturn(false);

        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveService.submit(employeeId, request));
    }

    @Test
    @DisplayName("Approve: Berhasil menyetujui cuti dan memotong saldo dengan tepat")
    void approve_Success_DeductsBalanceExactly() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(3)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(employeeRepository.findByIdWithLock(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestResponse response = leaveService.approve(requestId, managerId);

        assertNotNull(response);
        assertEquals(LeaveStatus.APPROVED, response.getStatus());
        assertEquals(managerId, response.getApprovedById());
        // Saldo awal 10 - 3 = 7
        assertEquals(7, employee.getLeaveBalance());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Approve: Gagal 409 jika status bukan PENDING (approve dua kali)")
    void approve_ThrowsInvalidStatusTransition_WhenNotPending() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(3)
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));

        assertThrows(InvalidStatusTransitionException.class, () -> leaveService.approve(requestId, managerId));
    }

    @Test
    @DisplayName("Approve: Gagal 403 jika approver adalah pemohon sendiri (self-approval)")
    void approve_ThrowsAccessDenied_WhenApproverIsApplicantThemselves() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(2)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(AccessDeniedException.class, () -> leaveService.approve(requestId, employeeId));
    }

    @Test
    @DisplayName("Approve: Gagal 403 jika approver bukan direct manager dan bukan HR")
    void approve_ThrowsAccessDenied_WhenApproverNotDirectManagerAndNotHR() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(2)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(otherManagerId)).thenReturn(Optional.of(otherManager));

        assertThrows(AccessDeniedException.class, () -> leaveService.approve(requestId, otherManagerId));
    }

    @Test
    @DisplayName("Approve: Berhasil jika disetujui oleh HR (meskipun bukan direct manager)")
    void approve_Success_WhenApproverIsHR() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(2)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(hrId)).thenReturn(Optional.of(hr));
        when(employeeRepository.findByIdWithLock(employeeId)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestResponse response = leaveService.approve(requestId, hrId);

        assertNotNull(response);
        assertEquals(LeaveStatus.APPROVED, response.getStatus());
        assertEquals(hrId, response.getApprovedById());
        assertEquals(8, employee.getLeaveBalance());
    }

    @Test
    @DisplayName("Approve: Gagal 409 jika saat dicek ulang saldo sudah tidak mencukupi")
    void approve_ThrowsInsufficientBalance_WhenBalanceChangedBeforeApproval() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(5)
                .status(LeaveStatus.PENDING)
                .build();

        // Misal saldo tersisa hanya 3 hari karena pengajuan lain sudah di-approve
        employee.setLeaveBalance(3);

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(employeeRepository.findByIdWithLock(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(InsufficientLeaveBalanceException.class, () -> leaveService.approve(requestId, managerId));
    }

    @Test
    @DisplayName("Reject: Berhasil menolak cuti dan saldo karyawan tidak berkurang")
    void reject_Success_LeavesBalanceUnchanged() {
        UUID requestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .requestedDays(4)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestResponse response = leaveService.reject(requestId, managerId);

        assertNotNull(response);
        assertEquals(LeaveStatus.REJECTED, response.getStatus());
        assertEquals(managerId, response.getApprovedById());
        // Saldo tetap 10
        assertEquals(10, employee.getLeaveBalance());
        verify(employeeRepository, never()).findByIdWithLock(any());
    }
}
