package com.elms.backend.performance;

import com.elms.backend.common.exception.DuplicateResourceException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.common.exception.ReviewAlreadyExistsException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.LeaveService;
import com.elms.backend.performance.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final ReviewPeriodRepository reviewPeriodRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Menghitung nilai overall score sebagai rata-rata dari 4 aspek penilaian.
     */
    public BigDecimal calculateOverallScore(int technical, int communication, int teamwork, int problemSolving) {
        double avg = (technical + communication + teamwork + problemSolving) / 4.0;
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Review Period Methods (HR) ──────────────────────────────────

    @Transactional
    public ReviewPeriodResponse createPeriod(CreateReviewPeriodRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Tanggal mulai periode tidak boleh lebih besar dari tanggal selesai");
        }

        String trimmedName = request.getName().trim();
        if (reviewPeriodRepository.existsByName(trimmedName)) {
            throw new DuplicateResourceException("Periode evaluasi dengan nama '" + trimmedName + "' sudah ada");
        }

        ReviewPeriod period = ReviewPeriod.builder()
                .name(trimmedName)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        ReviewPeriod saved = reviewPeriodRepository.save(period);
        log.info("Review period created: id={}, name={}", saved.getId(), saved.getName());
        return ReviewPeriodResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewPeriodResponse> getAllPeriods() {
        return reviewPeriodRepository.findAllByOrderByStartDateDesc().stream()
                .map(ReviewPeriodResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewPeriodResponse getPeriodById(Long id) {
        ReviewPeriod period = reviewPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Periode evaluasi tidak ditemukan dengan id: " + id));
        return ReviewPeriodResponse.fromEntity(period);
    }

    // ── Performance Review Methods (Manager) ─────────────────────────

    @Transactional
    public PerformanceReviewResponse createReview(UUID reviewerId, CreatePerformanceReviewRequest request) {
        ReviewPeriod period = reviewPeriodRepository.findById(request.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Periode evaluasi tidak ditemukan dengan id: " + request.getPeriodId()));

        LocalDate today = LocalDate.now(LeaveService.ZONE_JAKARTA);
        if (today.isBefore(period.getStartDate()) || today.isAfter(period.getEndDate())) {
            throw new IllegalStateException(String.format(
                    "Evaluasi kinerja hanya dapat diisi selama rentang periode evaluasi aktif (%s s/d %s)",
                    period.getStartDate(), period.getEndDate()));
        }

        if (reviewerId.equals(request.getEmployeeId())) {
            throw new AccessDeniedException("Anda tidak diperbolehkan mereview diri Anda sendiri");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan dengan id: " + request.getEmployeeId()));

        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer tidak ditemukan dengan id: " + reviewerId));

        if (employee.getManager() == null || !employee.getManager().getId().equals(reviewerId)) {
            throw new AccessDeniedException("Anda hanya dapat mengisi review untuk anggota tim langsung Anda");
        }

        if (performanceReviewRepository.existsByReviewPeriodIdAndEmployeeId(period.getId(), employee.getId())) {
            throw new ReviewAlreadyExistsException("Review evaluasi kinerja untuk karyawan ini pada periode tersebut sudah ada");
        }

        BigDecimal overall = calculateOverallScore(
                request.getTechnicalSkill(),
                request.getCommunication(),
                request.getTeamwork(),
                request.getProblemSolving()
        );

        PerformanceReview review = PerformanceReview.builder()
                .reviewPeriod(period)
                .employee(employee)
                .reviewer(reviewer)
                .technicalSkill(request.getTechnicalSkill())
                .communication(request.getCommunication())
                .teamwork(request.getTeamwork())
                .problemSolving(request.getProblemSolving())
                .overallScore(overall)
                .comments(request.getComments() != null ? request.getComments().trim() : "")
                .build();

        PerformanceReview saved = performanceReviewRepository.save(review);
        log.info("Performance review created: id={}, employeeId={}, reviewerId={}, overallScore={}",
                saved.getId(), employee.getId(), reviewerId, overall);

        return PerformanceReviewResponse.fromEntity(saved);
    }

    @Transactional
    public PerformanceReviewResponse updateReview(UUID reviewerId, UUID reviewId, UpdatePerformanceReviewRequest request) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review tidak ditemukan dengan id: " + reviewId));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new AccessDeniedException("Hanya manager yang membuat evaluasi ini yang dapat mengubahnya");
        }

        ReviewPeriod period = review.getReviewPeriod();
        LocalDate today = LocalDate.now(LeaveService.ZONE_JAKARTA);
        if (today.isBefore(period.getStartDate()) || today.isAfter(period.getEndDate())) {
            throw new IllegalStateException("Evaluasi tidak dapat diubah karena periode evaluasi sudah berakhir atau belum aktif");
        }

        BigDecimal overall = calculateOverallScore(
                request.getTechnicalSkill(),
                request.getCommunication(),
                request.getTeamwork(),
                request.getProblemSolving()
        );

        review.setTechnicalSkill(request.getTechnicalSkill());
        review.setCommunication(request.getCommunication());
        review.setTeamwork(request.getTeamwork());
        review.setProblemSolving(request.getProblemSolving());
        review.setOverallScore(overall);
        review.setComments(request.getComments() != null ? request.getComments().trim() : "");

        PerformanceReview saved = performanceReviewRepository.save(review);
        log.info("Performance review updated: id={}, overallScore={}", saved.getId(), overall);

        return PerformanceReviewResponse.fromEntity(saved);
    }

    // ── Queries ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PerformanceReviewResponse> getMyReviews(UUID employeeId, Pageable pageable) {
        Specification<PerformanceReview> spec = PerformanceReviewSpecification.withEmployeeId(employeeId);
        return performanceReviewRepository.findAll(spec, pageable)
                .map(PerformanceReviewResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PerformanceReviewResponse> getTeamReviews(
            UUID viewerId,
            Role viewerRole,
            Long periodId,
            UUID employeeId,
            Long departmentId,
            Pageable pageable) {

        Specification<PerformanceReview> spec;

        if (viewerRole == Role.HR) {
            spec = Specification.allOf(
                    PerformanceReviewSpecification.withPeriodId(periodId),
                    PerformanceReviewSpecification.withEmployeeId(employeeId),
                    PerformanceReviewSpecification.withDepartmentId(departmentId)
            );
        } else if (viewerRole == Role.MANAGER) {
            spec = Specification.allOf(
                    PerformanceReviewSpecification.withReviewerId(viewerId),
                    PerformanceReviewSpecification.withPeriodId(periodId),
                    PerformanceReviewSpecification.withEmployeeId(employeeId),
                    PerformanceReviewSpecification.withDepartmentId(departmentId)
            );
        } else {
            throw new AccessDeniedException("Hanya Manager dan HR yang dapat mengakses daftar evaluasi kinerja tim");
        }

        return performanceReviewRepository.findAll(spec, pageable)
                .map(PerformanceReviewResponse::fromEntity);
    }
}
