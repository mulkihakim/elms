package com.elms.backend.common.seeder;

import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import com.elms.backend.organization.department.Department;
import com.elms.backend.organization.department.DepartmentRepository;
import com.elms.backend.organization.position.Position;
import com.elms.backend.organization.position.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Seed data untuk development.
 * Hanya aktif jika profile "dev" dijalankan (spring.profiles.active=dev).
 * Mengisi tabel departments, positions, & employees dengan data awal,
 * HANYA jika tabel masih kosong.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final com.elms.backend.attendance.AttendanceRepository attendanceRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedOrganization();
        seedEmployees();
        seedAttendances();
    }

    private void seedOrganization() {
        if (departmentRepository.count() > 0) {
            log.info("⏭️  Organization seed skipped — departments already exist.");
            return;
        }

        log.info("🌱 Seeding departments & positions...");

        // ── Departments ──────────────────────────────────────────
        Department engineering = departmentRepository.save(
                Department.builder().name("Engineering").build());
        Department hr = departmentRepository.save(
                Department.builder().name("Human Resources").build());
        Department finance = departmentRepository.save(
                Department.builder().name("Finance").build());
        Department marketing = departmentRepository.save(
                Department.builder().name("Marketing").build());
        Department operations = departmentRepository.save(
                Department.builder().name("Operations").build());

        log.info("✅ {} departments seeded.", departmentRepository.count());

        // ── Positions ────────────────────────────────────────────
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

        log.info("✅ {} positions seeded.", positionRepository.count());
    }

    private void seedEmployees() {
        if (employeeRepository.count() > 0) {
            log.info("⏭️  Employee seed skipped — employees already exist.");
            return;
        }

        log.info("🌱 Seeding initial employees (HR, Manager, Staff)...");

        Department hrDept = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase("Human Resources"))
                .findFirst()
                .orElse(null);

        Department engDept = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase("Engineering"))
                .findFirst()
                .orElse(null);

        if (hrDept == null || engDept == null) {
            log.warn("⚠️ Cannot seed employees: HR or Engineering department not found.");
            return;
        }

        List<Position> hrPositions = positionRepository.findByDepartmentId(hrDept.getId());
        List<Position> engPositions = positionRepository.findByDepartmentId(engDept.getId());

        Position hrManagerPos = hrPositions.stream()
                .filter(p -> p.getTitle().toLowerCase().contains("manager"))
                .findFirst()
                .orElse(hrPositions.isEmpty() ? null : hrPositions.get(0));

        Position engManagerPos = engPositions.stream()
                .filter(p -> p.getTitle().toLowerCase().contains("manager"))
                .findFirst()
                .orElse(engPositions.isEmpty() ? null : engPositions.get(0));

        Position backendPos = engPositions.stream()
                .filter(p -> p.getTitle().toLowerCase().contains("backend") || p.getTitle().toLowerCase().contains("engineer"))
                .findFirst()
                .orElse(engPositions.isEmpty() ? null : engPositions.get(0));

        Position frontendPos = engPositions.stream()
                .filter(p -> p.getTitle().toLowerCase().contains("frontend") || p.getTitle().toLowerCase().contains("developer"))
                .findFirst()
                .orElse(backendPos);

        if (hrManagerPos == null || engManagerPos == null || backendPos == null) {
            log.warn("⚠️ Cannot seed employees: Required positions not found.");
            return;
        }

        String defaultHashedPassword = passwordEncoder.encode("password123");

        // 1. HR Admin (Budi Santoso)
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

        // 2. Engineering Manager (Siti Rahma)
        Employee sitiManager = employeeRepository.save(Employee.builder()
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

        // 3. Staff Employee 1 (Ahmad Fauzi - supervised by Siti Rahma)
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

        // 4. Staff Employee 2 (Dewi Lestari - supervised by Siti Rahma)
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

        log.info("✅ {} employees seeded.", employeeRepository.count());
    }

    private void seedAttendances() {
        if (attendanceRepository.count() > 0) {
            log.info("⏭️  Attendance seed skipped — attendances already exist.");
            return;
        }

        log.info("🌱 Seeding sample attendance records...");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Employee budi = employeeRepository.findByEmail("budi.hr@elms.com").orElse(null);
        Employee siti = employeeRepository.findByEmail("siti.manager@elms.com").orElse(null);
        Employee ahmad = employeeRepository.findByEmail("ahmad.emp@elms.com").orElse(null);
        Employee dewi = employeeRepository.findByEmail("dewi.emp@elms.com").orElse(null);

        if (budi == null || siti == null || ahmad == null || dewi == null) {
            log.warn("⚠️ Cannot seed attendances: Required employees not found.");
            return;
        }

        // Budi (HR) - Hari ini hadir on time
        attendanceRepository.save(com.elms.backend.attendance.Attendance.builder()
                .employee(budi)
                .date(today)
                .checkIn(today.atTime(8, 45, 0))
                .status(com.elms.backend.attendance.AttendanceStatus.ON_TIME)
                .notes("Hadir di kantor")
                .build());

        // Siti (Manager) - Hari ini hadir on time
        attendanceRepository.save(com.elms.backend.attendance.Attendance.builder()
                .employee(siti)
                .date(today)
                .checkIn(today.atTime(8, 52, 0))
                .status(com.elms.backend.attendance.AttendanceStatus.ON_TIME)
                .build());

        // Ahmad (Staff) - Kemarin full day, hari ini terlambat
        attendanceRepository.save(com.elms.backend.attendance.Attendance.builder()
                .employee(ahmad)
                .date(yesterday)
                .checkIn(yesterday.atTime(8, 55, 0))
                .checkOut(yesterday.atTime(17, 15, 0))
                .status(com.elms.backend.attendance.AttendanceStatus.ON_TIME)
                .workMinutes(500)
                .build());

        attendanceRepository.save(com.elms.backend.attendance.Attendance.builder()
                .employee(ahmad)
                .date(today)
                .checkIn(today.atTime(9, 18, 0))
                .status(com.elms.backend.attendance.AttendanceStatus.LATE)
                .notes("Terjebak macet di jalan")
                .build());

        // Dewi (Staff) - Kemarin hadir, hari ini belum check in
        attendanceRepository.save(com.elms.backend.attendance.Attendance.builder()
                .employee(dewi)
                .date(yesterday)
                .checkIn(yesterday.atTime(9, 4, 0))
                .checkOut(yesterday.atTime(17, 30, 0))
                .status(com.elms.backend.attendance.AttendanceStatus.LATE)
                .workMinutes(506)
                .build());

        log.info("✅ {} attendances seeded.", attendanceRepository.count());
    }
}
