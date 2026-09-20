package com.elms.backend.leave;

import com.elms.backend.common.exception.InsufficientLeaveBalanceException;
import com.elms.backend.common.exception.InvalidStatusTransitionException;
import com.elms.backend.common.exception.LeaveOverlapException;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.dto.CreateLeaveRequest;
import com.elms.backend.leave.dto.LeaveRequestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    public static final ZoneId ZONE_JAKARTA = ZoneId.of("Asia/Jakarta");

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Menghitung jumlah hari kerja (Senin s/d Jumat) antara dua tanggal inklusif.
     */
    public int calculateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }

        int workingDays = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays;
    }

    /**
     * Mengajukan permohonan cuti baru.
     */
    @Transactional
    public LeaveRequestResponse submit(UUID employeeId, CreateLeaveRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (employee.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new IllegalStateException("Hanya karyawan dengan status ACTIVE yang dapat mengajukan cuti");
        }

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Tanggal mulai dan tanggal selesai wajib diisi");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Tanggal mulai cuti tidak boleh lebih besar dari tanggal selesai");
        }

        LocalDate today = LocalDate.now(ZONE_JAKARTA);
        if (start.isBefore(today)) {
            throw new IllegalArgumentException("Tanggal mulai cuti tidak boleh di masa lampau (backdate)");
        }

        int requestedDays = calculateWorkingDays(start, end);
        if (requestedDays <= 0) {
            throw new IllegalArgumentException("Pengajuan cuti harus mencakup minimal 1 hari kerja (Senin–Jumat)");
        }

        boolean hasOverlap = leaveRequestRepository.existsOverlapping(
                employeeId,
                start,
                end,
                List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED)
        );
        if (hasOverlap) {
            throw new LeaveOverlapException("Terdapat pengajuan cuti lain (PENDING atau APPROVED) yang bersinggungan pada rentang tanggal tersebut");
        }

        if (requestedDays > employee.getLeaveBalance()) {
            throw new InsufficientLeaveBalanceException(
                    String.format("Sisa saldo cuti Anda tidak mencukupi (dibutuhkan %d hari, sisa %d hari)",
                            requestedDays, employee.getLeaveBalance())
            );
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType() != null ? request.getLeaveType() : LeaveType.ANNUAL)
                .startDate(start)
                .endDate(end)
                .requestedDays(requestedDays)
                .reason(request.getReason() != null ? request.getReason().trim() : "")
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request submitted successfully: id={}, employeeId={}, requestedDays={}",
                saved.getId(), employeeId, requestedDays);

        return LeaveRequestResponse.fromEntity(saved);
    }

    /**
     * Menyetujui pengajuan cuti (Approve) dalam transaksi dengan pessimistic lock pada saldo employee.
     */
    @Transactional
    public LeaveRequestResponse approve(UUID requestId, UUID approverId) {
        return decide(requestId, approverId, true);
    }

    /**
     * Menolak pengajuan cuti (Reject) dalam transaksi tanpa memotong saldo.
     */
    @Transactional
    public LeaveRequestResponse reject(UUID requestId, UUID approverId) {
        return decide(requestId, approverId, false);
    }

    /**
     * Logika sentral otorisasi dan pemutusan pengajuan cuti.
     */
    private LeaveRequestResponse decide(UUID requestId, UUID approverId, boolean isApprove) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + requestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    String.format("Pengajuan cuti sudah berstatus %s dan tidak dapat diubah lagi", leaveRequest.getStatus())
            );
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + approverId));

        Employee applicant = leaveRequest.getEmployee();

        // 1. Dilarang approve cuti milik sendiri
        if (applicant.getId().equals(approverId)) {
            throw new AccessDeniedException("Anda tidak diperbolehkan memutuskan pengajuan cuti Anda sendiri");
        }

        // 2. Otorisasi pemutus: HR atau Manager langsung
        boolean isHR = approver.getRole() == Role.HR;
        boolean isDirectManager = applicant.getManager() != null && applicant.getManager().getId().equals(approverId);

        if (!isHR && !isDirectManager) {
            throw new AccessDeniedException("Anda tidak memiliki wewenang untuk memutuskan pengajuan cuti karyawan ini");
        }

        if (isApprove) {
            // Lock baris employee pemohon (PESSIMISTIC_WRITE)
            Employee lockedApplicant = employeeRepository.findByIdWithLock(applicant.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + applicant.getId()));

            if (leaveRequest.getRequestedDays() > lockedApplicant.getLeaveBalance()) {
                throw new InsufficientLeaveBalanceException(
                        String.format("Persetujuan gagal: Sisa saldo cuti karyawan tidak mencukupi (dibutuhkan %d hari, sisa %d hari)",
                                leaveRequest.getRequestedDays(), lockedApplicant.getLeaveBalance())
                );
            }

            lockedApplicant.setLeaveBalance(lockedApplicant.getLeaveBalance() - leaveRequest.getRequestedDays());
            employeeRepository.save(lockedApplicant);
            leaveRequest.setStatus(LeaveStatus.APPROVED);
            log.info("Leave request approved: id={}, applicantId={}, deductedDays={}, remainingBalance={}",
                    requestId, applicant.getId(), leaveRequest.getRequestedDays(), lockedApplicant.getLeaveBalance());
        } else {
            leaveRequest.setStatus(LeaveStatus.REJECTED);
            log.info("Leave request rejected: id={}, applicantId={}", requestId, applicant.getId());
        }

        leaveRequest.setApprovedBy(approver);
        leaveRequest.setDecidedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestResponse.fromEntity(saved);
    }

    /**
     * Riwayat pengajuan cuti pribadi karyawan.
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getMyLeaveRequests(UUID employeeId, LeaveStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<LeaveRequest> spec = Specification.allOf(
                LeaveSpecification.withEmployeeId(employeeId),
                LeaveSpecification.withStatus(status),
                LeaveSpecification.withDateRange(from, to)
        );

        return leaveRequestRepository.findAll(spec, pageable)
                .map(LeaveRequestResponse::fromEntity);
    }

    /**
     * Daftar pengajuan cuti anggota tim (untuk Manager) atau seluruh organisasi (untuk HR).
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequestResponse> getTeamLeaveRequests(
            UUID approverId,
            Role approverRole,
            UUID employeeId,
            Long departmentId,
            LeaveStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        Specification<LeaveRequest> spec;

        if (approverRole == Role.HR) {
            // HR bisa melihat semua atau difilter spesifik
            spec = Specification.allOf(
                    LeaveSpecification.withEmployeeId(employeeId),
                    LeaveSpecification.withDepartmentId(departmentId),
                    LeaveSpecification.withStatus(status),
                    LeaveSpecification.withDateRange(from, to)
            );
        } else if (approverRole == Role.MANAGER) {
            // Manager hanya bisa melihat anggota timnya
            spec = Specification.allOf(
                    LeaveSpecification.withManagerId(approverId),
                    LeaveSpecification.withEmployeeId(employeeId),
                    LeaveSpecification.withDepartmentId(departmentId),
                    LeaveSpecification.withStatus(status),
                    LeaveSpecification.withDateRange(from, to)
            );
        } else {
            throw new AccessDeniedException("Hanya Manager dan HR yang dapat mengakses daftar pengajuan cuti tim");
        }

        return leaveRequestRepository.findAll(spec, pageable)
                .map(LeaveRequestResponse::fromEntity);
    }
}
