package com.elms.backend.common.seeder;

import com.elms.backend.attendance.Attendance;
import com.elms.backend.attendance.AttendanceRepository;
import com.elms.backend.attendance.AttendanceStatus;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.leave.LeaveRequest;
import com.elms.backend.leave.LeaveRequestRepository;
import com.elms.backend.leave.LeaveStatus;
import com.elms.backend.leave.LeaveType;
import com.elms.backend.organization.department.Department;
import com.elms.backend.organization.department.DepartmentRepository;
import com.elms.backend.organization.position.Position;
import com.elms.backend.organization.position.PositionRepository;
import com.elms.backend.performance.PerformanceReview;
import com.elms.backend.performance.PerformanceReviewRepository;
import com.elms.backend.performance.ReviewPeriod;
import com.elms.backend.performance.ReviewPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed data lengkap untuk keperluan demonstrasi, portfolio, dan development.
 * Hanya aktif jika profile "dev" dijalankan (spring.profiles.active=dev).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ReviewPeriodRepository reviewPeriodRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedOrganization();
        seedEmployees();
        seedAttendances();
        seedLeaves();
        seedPerformanceReviews();
        log.info("🎉 Database seeding completed successfully!");
    }

    private void seedOrganization() {
        if (departmentRepository.count() > 0) {
            log.info("⏭️  Organization seed skipped — departments already exist.");
            return;
        }

        log.info("🌱 Seeding departments & positions...");

        Department engineering = departmentRepository.save(Department.builder().name("Engineering").build());
        Department hr = departmentRepository.save(Department.builder().name("Human Resources").build());
        Department finance = departmentRepository.save(Department.builder().name("Finance").build());
        Department marketing = departmentRepository.save(Department.builder().name("Marketing").build());
        Department operations = departmentRepository.save(Department.builder().name("Operations").build());

        positionRepository.saveAll(List.of(
                // Engineering
                Position.builder().title("Engineering Manager").department(engineering).build(),
                Position.builder().title("Senior Backend Engineer").department(engineering).build(),
                Position.builder().title("Frontend Developer").department(engineering).build(),
                Position.builder().title("QA Engineer").department(engineering).build(),

                // Human Resources
                Position.builder().title("HR Manager").department(hr).build(),
                Position.builder().title("HR Specialist").department(hr).build(),
                Position.builder().title("Recruiter").department(hr).build(),

                // Finance
                Position.builder().title("Finance Lead").department(finance).build(),
                Position.builder().title("Accountant").department(finance).build(),

                // Marketing
                Position.builder().title("Marketing Lead").department(marketing).build(),
                Position.builder().title("Content Strategist").department(marketing).build(),

                // Operations
                Position.builder().title("Operations Specialist").department(operations).build()
        ));

        log.info("✅ {} departments and {} positions seeded.", departmentRepository.count(), positionRepository.count());
    }

    private void seedEmployees() {
        Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);
        Department engDept = departmentRepository.findByName("Engineering").orElse(null);
        Department finDept = departmentRepository.findByName("Finance").orElse(null);
        Department mktDept = departmentRepository.findByName("Marketing").orElse(null);
        Department opsDept = departmentRepository.findByName("Operations").orElse(null);

        if (hrDept == null || engDept == null) {
            log.warn("⚠️ Cannot seed employees: Master departments missing.");
            return;
        }

        String defaultHashedPassword = passwordEncoder.encode("password123");

        // Helper position finders
        Position hrManagerPos = getPositionByDeptAndKeyword(hrDept.getId(), "Manager");
        Position engManagerPos = getPositionByDeptAndKeyword(engDept.getId(), "Manager");
        Position backendPos = getPositionByDeptAndKeyword(engDept.getId(), "Backend");
        Position frontendPos = getPositionByDeptAndKeyword(engDept.getId(), "Frontend");
        Position qaPos = getPositionByDeptAndKeyword(engDept.getId(), "QA");

        Position finLeadPos = finDept != null ? getPositionByDeptAndKeyword(finDept.getId(), "Lead") : null;
        Position accountantPos = finDept != null ? getPositionByDeptAndKeyword(finDept.getId(), "Accountant") : null;

        Position mktLeadPos = mktDept != null ? getPositionByDeptAndKeyword(mktDept.getId(), "Lead") : null;
        Position contentPos = mktDept != null ? getPositionByDeptAndKeyword(mktDept.getId(), "Content") : null;

        Position opsPos = opsDept != null ? getPositionByDeptAndKeyword(opsDept.getId(), "Operations") : null;

        // 1. HR Manager (Budi Santoso)
        if (!employeeRepository.existsByEmail("budi.hr@elms.com")) {
            employeeRepository.save(Employee.builder()
                    .fullName("Budi Santoso")
                    .email("budi.hr@elms.com")
                    .phone("+628111222333")
                    .department(hrDept)
                    .position(hrManagerPos)
                    .manager(null)
                    .joinDate(LocalDate.of(2024, 1, 15))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.HR)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 2. Engineering Manager (Siti Rahma)
        Employee sitiManager = employeeRepository.findByEmail("siti.manager@elms.com").orElse(null);
        if (sitiManager == null) {
            sitiManager = employeeRepository.save(Employee.builder()
                    .fullName("Siti Rahma")
                    .email("siti.manager@elms.com")
                    .phone("+628111222444")
                    .department(engDept)
                    .position(engManagerPos)
                    .manager(null)
                    .joinDate(LocalDate.of(2024, 2, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.MANAGER)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 3. Backend Staff (Ahmad Fauzi) - bawahan Siti
        if (!employeeRepository.existsByEmail("ahmad.emp@elms.com")) {
            employeeRepository.save(Employee.builder()
                    .fullName("Ahmad Fauzi")
                    .email("ahmad.emp@elms.com")
                    .phone("+628111222555")
                    .department(engDept)
                    .position(backendPos)
                    .manager(sitiManager)
                    .joinDate(LocalDate.of(2024, 3, 10))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 4. Frontend Staff (Dewi Lestari) - bawahan Siti
        if (!employeeRepository.existsByEmail("dewi.emp@elms.com")) {
            employeeRepository.save(Employee.builder()
                    .fullName("Dewi Lestari")
                    .email("dewi.emp@elms.com")
                    .phone("+628111222666")
                    .department(engDept)
                    .position(frontendPos != null ? frontendPos : backendPos)
                    .manager(sitiManager)
                    .joinDate(LocalDate.of(2024, 4, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(10)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 5. QA Staff (Rizky Pratama) - bawahan Siti
        if (!employeeRepository.existsByEmail("rizky.emp@elms.com")) {
            employeeRepository.save(Employee.builder()
                    .fullName("Rizky Pratama")
                    .email("rizky.emp@elms.com")
                    .phone("+628111222777")
                    .department(engDept)
                    .position(qaPos != null ? qaPos : backendPos)
                    .manager(sitiManager)
                    .joinDate(LocalDate.of(2024, 5, 15))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 6. Finance Lead (Eko Prasetyo)
        Employee ekoManager = employeeRepository.findByEmail("eko.manager@elms.com").orElse(null);
        if (ekoManager == null && finDept != null) {
            ekoManager = employeeRepository.save(Employee.builder()
                    .fullName("Eko Prasetyo")
                    .email("eko.manager@elms.com")
                    .phone("+628111222888")
                    .department(finDept)
                    .position(finLeadPos)
                    .manager(null)
                    .joinDate(LocalDate.of(2024, 2, 10))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.MANAGER)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 7. Finance Staff (Fitri Handayani) - bawahan Eko
        if (!employeeRepository.existsByEmail("fitri.emp@elms.com") && finDept != null) {
            employeeRepository.save(Employee.builder()
                    .fullName("Fitri Handayani")
                    .email("fitri.emp@elms.com")
                    .phone("+628111222999")
                    .department(finDept)
                    .position(accountantPos != null ? accountantPos : finLeadPos)
                    .manager(ekoManager)
                    .joinDate(LocalDate.of(2024, 3, 20))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(9)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 8. Marketing Lead (Gilang Ramadhan)
        Employee gilangManager = employeeRepository.findByEmail("gilang.manager@elms.com").orElse(null);
        if (gilangManager == null && mktDept != null) {
            gilangManager = employeeRepository.save(Employee.builder()
                    .fullName("Gilang Ramadhan")
                    .email("gilang.manager@elms.com")
                    .phone("+628111333111")
                    .department(mktDept)
                    .position(mktLeadPos)
                    .manager(null)
                    .joinDate(LocalDate.of(2024, 3, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.MANAGER)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 9. Marketing Staff (Hana Kusuma) - bawahan Gilang
        if (!employeeRepository.existsByEmail("hana.emp@elms.com") && mktDept != null) {
            employeeRepository.save(Employee.builder()
                    .fullName("Hana Kusuma")
                    .email("hana.emp@elms.com")
                    .phone("+628111333222")
                    .department(mktDept)
                    .position(contentPos != null ? contentPos : mktLeadPos)
                    .manager(gilangManager)
                    .joinDate(LocalDate.of(2024, 4, 15))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(12)
                    .password(defaultHashedPassword)
                    .build());
        }

        // 10. Operations Staff (Indra Wijaya)
        Employee budiHr = employeeRepository.findByEmail("budi.hr@elms.com").orElse(null);
        if (!employeeRepository.existsByEmail("indra.emp@elms.com") && opsDept != null) {
            employeeRepository.save(Employee.builder()
                    .fullName("Indra Wijaya")
                    .email("indra.emp@elms.com")
                    .phone("+628111333333")
                    .department(opsDept)
                    .position(opsPos)
                    .manager(budiHr)
                    .joinDate(LocalDate.of(2024, 5, 1))
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .role(Role.EMPLOYEE)
                    .leaveBalance(11)
                    .password(defaultHashedPassword)
                    .build());
        }

        log.info("✅ {} employees seeded.", employeeRepository.count());
    }

    private void seedAttendances() {
        if (attendanceRepository.count() > 10) {
            log.info("⏭️  Attendance seed skipped — sufficient attendances already exist.");
            return;
        }

        log.info("🌱 Seeding realistic 7-day attendance records...");

        LocalDate today = LocalDate.now();
        List<Employee> allEmployees = employeeRepository.findAll();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            for (Employee emp : allEmployees) {
                if (attendanceRepository.existsByEmployeeIdAndDate(emp.getId(), date)) {
                    continue;
                }

                // Randomize / simulate attendance status based on employee
                boolean isLate = (emp.getFullName().contains("Ahmad") && i % 2 == 1) || (emp.getFullName().contains("Hana") && i == 2);
                boolean isAbsent = (emp.getFullName().contains("Fitri") && i <= 1); // Fitri is on leave

                if (isAbsent) {
                    continue;
                }

                int checkInMinute = isLate ? (10 + (i * 3) % 20) : (40 + (i * 2) % 15);
                int checkInHour = isLate ? 9 : 8;

                Attendance attendance = Attendance.builder()
                        .employee(emp)
                        .date(date)
                        .checkIn(date.atTime(checkInHour, checkInMinute, 0))
                        .checkOut(i > 0 ? date.atTime(17, 30, 0) : null)
                        .status(isLate ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME)
                        .workMinutes(i > 0 ? 510 : null)
                        .notes(isLate ? "Terlambat karena kendala transportasi" : null)
                        .build();

                attendanceRepository.save(attendance);
            }
        }

        log.info("✅ {} total attendances seeded.", attendanceRepository.count());
    }

    private void seedLeaves() {
        if (leaveRequestRepository.count() > 0) {
            log.info("⏭️  Leave seed skipped — leave requests already exist.");
            return;
        }

        log.info("🌱 Seeding sample leave requests (Approved, Pending, Rejected)...");

        Employee fitri = employeeRepository.findByEmail("fitri.emp@elms.com").orElse(null);
        Employee ahmad = employeeRepository.findByEmail("ahmad.emp@elms.com").orElse(null);
        Employee dewi = employeeRepository.findByEmail("dewi.emp@elms.com").orElse(null);
        Employee hana = employeeRepository.findByEmail("hana.emp@elms.com").orElse(null);
        Employee sitiManager = employeeRepository.findByEmail("siti.manager@elms.com").orElse(null);
        Employee ekoManager = employeeRepository.findByEmail("eko.manager@elms.com").orElse(null);

        LocalDate today = LocalDate.now();

        // 1. Cuti Aktif Hari Ini (Fitri Handayani - APPROVED)
        if (fitri != null && ekoManager != null) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(fitri)
                    .leaveType(LeaveType.ANNUAL)
                    .startDate(today.minusDays(1))
                    .endDate(today.plusDays(1))
                    .requestedDays(3)
                    .reason("Keperluan keluarga di luar kota")
                    .status(LeaveStatus.APPROVED)
                    .approvedBy(ekoManager)
                    .decidedAt(LocalDateTime.now().minusDays(2))
                    .build());
        }

        // 2. Cuti Menunggu Persetujuan Tim Siti (Ahmad Fauzi - PENDING)
        if (ahmad != null) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(ahmad)
                    .leaveType(LeaveType.ANNUAL)
                    .startDate(today.plusDays(3))
                    .endDate(today.plusDays(4))
                    .requestedDays(2)
                    .reason("Liburan akhir pekan panjang bersama keluarga")
                    .status(LeaveStatus.PENDING)
                    .build());
        }

        // 3. Cuti Menunggu Persetujuan (Hana Kusuma - PENDING)
        if (hana != null) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(hana)
                    .leaveType(LeaveType.SICK)
                    .startDate(today.plusDays(1))
                    .endDate(today.plusDays(2))
                    .requestedDays(2)
                    .reason("Jadwal pemeriksaan medis berkala")
                    .status(LeaveStatus.PENDING)
                    .build());
        }

        // 4. Cuti Ditolak Sebelumnya (Dewi Lestari - REJECTED)
        if (dewi != null && sitiManager != null) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(dewi)
                    .leaveType(LeaveType.ANNUAL)
                    .startDate(today.minusDays(10))
                    .endDate(today.minusDays(9))
                    .requestedDays(2)
                    .reason("Cuti mendadak saat sprint release")
                    .status(LeaveStatus.REJECTED)
                    .approvedBy(sitiManager)
                    .decidedAt(LocalDateTime.now().minusDays(11))
                    .build());
        }

        log.info("✅ {} leave requests seeded.", leaveRequestRepository.count());
    }

    private void seedPerformanceReviews() {
        if (reviewPeriodRepository.count() > 0) {
            log.info("⏭️  Performance review seed skipped — periods already exist.");
            return;
        }

        log.info("🌱 Seeding review periods and performance reviews...");

        LocalDate today = LocalDate.now();

        // Periode Aktif
        ReviewPeriod currentPeriod = reviewPeriodRepository.save(ReviewPeriod.builder()
                .name("Q1 2026")
                .startDate(today.minusDays(20))
                .endDate(today.plusDays(20))
                .build());

        // Periode Selesai
        reviewPeriodRepository.save(ReviewPeriod.builder()
                .name("Q4 2025")
                .startDate(today.minusDays(110))
                .endDate(today.minusDays(21))
                .build());

        Employee sitiManager = employeeRepository.findByEmail("siti.manager@elms.com").orElse(null);
        Employee ahmad = employeeRepository.findByEmail("ahmad.emp@elms.com").orElse(null);
        Employee dewi = employeeRepository.findByEmail("dewi.emp@elms.com").orElse(null);

        if (sitiManager != null && ahmad != null) {
            performanceReviewRepository.save(PerformanceReview.builder()
                    .reviewPeriod(currentPeriod)
                    .employee(ahmad)
                    .reviewer(sitiManager)
                    .technicalSkill(5)
                    .communication(4)
                    .teamwork(5)
                    .problemSolving(5)
                    .overallScore(new BigDecimal("4.75"))
                    .comments("Ahmad menunjukkan kemampuan teknis yang luar biasa dalam mendesain arsitektur backend dan konsisten tepat waktu.")
                    .build());
        }

        if (sitiManager != null && dewi != null) {
            performanceReviewRepository.save(PerformanceReview.builder()
                    .reviewPeriod(currentPeriod)
                    .employee(dewi)
                    .reviewer(sitiManager)
                    .technicalSkill(4)
                    .communication(5)
                    .teamwork(5)
                    .problemSolving(4)
                    .overallScore(new BigDecimal("4.50"))
                    .comments("Dewi sangat proaktif dalam integrasi komponen UI Vue 3 dan memiliki komunikasi lintas tim yang sangat baik.")
                    .build());
        }

        log.info("✅ {} review periods and {} performance reviews seeded.", reviewPeriodRepository.count(), performanceReviewRepository.count());
    }

    private Position getPositionByDeptAndKeyword(Long departmentId, String keyword) {
        List<Position> positions = positionRepository.findByDepartmentId(departmentId);
        return positions.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(positions.isEmpty() ? null : positions.get(0));
    }
}
