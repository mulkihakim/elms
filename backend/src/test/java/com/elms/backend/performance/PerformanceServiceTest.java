package com.elms.backend.performance;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ReviewAlreadyExistsException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.performance.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private ReviewPeriodRepository reviewPeriodRepository;

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PerformanceService performanceService;

    private UUID managerId;
    private UUID employeeId;
    private UUID otherManagerId;

    private Employee manager;
    private Employee employee;
    private Employee otherManager;
    private ReviewPeriod activePeriod;

    @BeforeEach
    void setUp() {
        managerId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        otherManagerId = UUID.randomUUID();

        manager = Employee.builder()
                .id(managerId)
                .fullName("Budi Manager")
                .role(Role.MANAGER)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        employee = Employee.builder()
                .id(employeeId)
                .fullName("Siti Staff")
                .role(Role.EMPLOYEE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .manager(manager)
                .build();

        otherManager = Employee.builder()
                .id(otherManagerId)
                .fullName("Iwan Other Manager")
                .role(Role.MANAGER)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        activePeriod = ReviewPeriod.builder()
                .id(1L)
                .name("Q1 2026")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .build();
    }

    @Test
    @DisplayName("Kalkulasi Skor: Overall score dihitung tepat dengan 2 desimal")
    void calculateOverallScore_Precision() {
        BigDecimal score1 = performanceService.calculateOverallScore(4, 5, 4, 4);
        assertEquals(new BigDecimal("4.25"), score1);

        BigDecimal score2 = performanceService.calculateOverallScore(3, 4, 4, 4);
        assertEquals(new BigDecimal("3.75"), score2);

        BigDecimal score3 = performanceService.calculateOverallScore(5, 5, 5, 5);
        assertEquals(new BigDecimal("5.00"), score3);
    }

    @Test
    @DisplayName("Create Review: Sukses membuat review oleh atasan langsung pada periode aktif")
    void createReview_Success() {
        CreatePerformanceReviewRequest request = CreatePerformanceReviewRequest.builder()
                .periodId(1L)
                .employeeId(employeeId)
                .technicalSkill(4)
                .communication(5)
                .teamwork(4)
                .problemSolving(4)
                .comments("Kinerja sangat baik dan memuaskan.")
                .build();

        when(reviewPeriodRepository.findById(1L)).thenReturn(Optional.of(activePeriod));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(performanceReviewRepository.existsByReviewPeriodIdAndEmployeeId(1L, employeeId)).thenReturn(false);
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenAnswer(inv -> {
            PerformanceReview r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        PerformanceReviewResponse response = performanceService.createReview(managerId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("4.25"), response.getOverallScore());
        assertEquals("Kinerja sangat baik dan memuaskan.", response.getComments());
        assertEquals(employeeId, response.getEmployeeId());
        assertEquals(managerId, response.getReviewerId());
    }

    @Test
    @DisplayName("Create Review: Gagal 403 jika reviewer bukan atasan langsung")
    void createReview_ThrowsAccessDenied_WhenReviewerNotDirectManager() {
        CreatePerformanceReviewRequest request = CreatePerformanceReviewRequest.builder()
                .periodId(1L)
                .employeeId(employeeId)
                .technicalSkill(4)
                .communication(4)
                .teamwork(4)
                .problemSolving(4)
                .build();

        when(reviewPeriodRepository.findById(1L)).thenReturn(Optional.of(activePeriod));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(otherManagerId)).thenReturn(Optional.of(otherManager));

        assertThrows(AccessDeniedException.class, () -> performanceService.createReview(otherManagerId, request));
    }

    @Test
    @DisplayName("Create Review: Gagal 403 jika mencoba mereview diri sendiri")
    void createReview_ThrowsAccessDenied_WhenReviewerIsEmployeeThemselves() {
        CreatePerformanceReviewRequest request = CreatePerformanceReviewRequest.builder()
                .periodId(1L)
                .employeeId(employeeId)
                .technicalSkill(5)
                .communication(5)
                .teamwork(5)
                .problemSolving(5)
                .build();

        when(reviewPeriodRepository.findById(1L)).thenReturn(Optional.of(activePeriod));

        assertThrows(AccessDeniedException.class, () -> performanceService.createReview(employeeId, request));
    }

    @Test
    @DisplayName("Create Review: Gagal 409 jika sudah ada review untuk periode ini")
    void createReview_ThrowsReviewAlreadyExists_WhenDuplicate() {
        CreatePerformanceReviewRequest request = CreatePerformanceReviewRequest.builder()
                .periodId(1L)
                .employeeId(employeeId)
                .technicalSkill(4)
                .communication(4)
                .teamwork(4)
                .problemSolving(4)
                .build();

        when(reviewPeriodRepository.findById(1L)).thenReturn(Optional.of(activePeriod));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(performanceReviewRepository.existsByReviewPeriodIdAndEmployeeId(1L, employeeId)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class, () -> performanceService.createReview(managerId, request));
    }

    @Test
    @DisplayName("Create Review: Gagal jika periode belum dimulai atau sudah lewat")
    void createReview_ThrowsIllegalState_WhenPeriodNotActive() {
        ReviewPeriod expiredPeriod = ReviewPeriod.builder()
                .id(2L)
                .name("Q4 2025")
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(30))
                .build();

        CreatePerformanceReviewRequest request = CreatePerformanceReviewRequest.builder()
                .periodId(2L)
                .employeeId(employeeId)
                .technicalSkill(4)
                .communication(4)
                .teamwork(4)
                .problemSolving(4)
                .build();

        when(reviewPeriodRepository.findById(2L)).thenReturn(Optional.of(expiredPeriod));

        assertThrows(IllegalStateException.class, () -> performanceService.createReview(managerId, request));
    }

    @Test
    @DisplayName("Update Review: Sukses memperbarui review oleh manager yang sama")
    void updateReview_Success() {
        UUID reviewId = UUID.randomUUID();
        PerformanceReview existing = PerformanceReview.builder()
                .id(reviewId)
                .reviewPeriod(activePeriod)
                .employee(employee)
                .reviewer(manager)
                .technicalSkill(3)
                .communication(3)
                .teamwork(3)
                .problemSolving(3)
                .overallScore(new BigDecimal("3.00"))
                .build();

        UpdatePerformanceReviewRequest request = UpdatePerformanceReviewRequest.builder()
                .technicalSkill(5)
                .communication(5)
                .teamwork(4)
                .problemSolving(4)
                .comments("Peningkatan performa yang sangat signifikan.")
                .build();

        when(performanceReviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenAnswer(inv -> inv.getArgument(0));

        PerformanceReviewResponse response = performanceService.updateReview(managerId, reviewId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("4.50"), response.getOverallScore());
        assertEquals("Peningkatan performa yang sangat signifikan.", response.getComments());
    }

    @Test
    @DisplayName("Update Review: Gagal 403 jika diubah oleh manager yang berbeda")
    void updateReview_ThrowsAccessDenied_WhenDifferentReviewer() {
        UUID reviewId = UUID.randomUUID();
        PerformanceReview existing = PerformanceReview.builder()
                .id(reviewId)
                .reviewPeriod(activePeriod)
                .employee(employee)
                .reviewer(manager)
                .build();

        UpdatePerformanceReviewRequest request = UpdatePerformanceReviewRequest.builder()
                .technicalSkill(5)
                .communication(5)
                .teamwork(4)
                .problemSolving(4)
                .build();

        when(performanceReviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> performanceService.updateReview(otherManagerId, reviewId, request));
    }

    @Test
    @DisplayName("Create Period: Sukses membuat periode evaluasi oleh HR")
    void createPeriod_Success() {
        CreateReviewPeriodRequest request = CreateReviewPeriodRequest.builder()
                .name("Q2 2026")
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        when(reviewPeriodRepository.existsByName("Q2 2026")).thenReturn(false);
        when(reviewPeriodRepository.save(any(ReviewPeriod.class))).thenAnswer(inv -> {
            ReviewPeriod p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        ReviewPeriodResponse response = performanceService.createPeriod(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Q2 2026", response.getName());
    }

    @Test
    @DisplayName("Create Period: Gagal jika startDate > endDate")
    void createPeriod_ThrowsIllegalArgument_WhenStartDateAfterEndDate() {
        CreateReviewPeriodRequest request = CreateReviewPeriodRequest.builder()
                .name("Q3 2026")
                .startDate(LocalDate.now().plusMonths(3))
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> performanceService.createPeriod(request));
    }

    @Test
    @DisplayName("Create Period: Gagal 409 jika nama periode sudah ada")
    void createPeriod_ThrowsDuplicateResource_WhenNameExists() {
        CreateReviewPeriodRequest request = CreateReviewPeriodRequest.builder()
                .name("Q1 2026")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        when(reviewPeriodRepository.existsByName("Q1 2026")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> performanceService.createPeriod(request));
    }
}
