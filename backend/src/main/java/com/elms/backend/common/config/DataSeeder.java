package com.elms.backend.common.config;

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

import java.util.List;

/**
 * Seed data untuk development.
 * Hanya aktif jika profile "dev" dijalankan (spring.profiles.active=dev).
 * Mengisi tabel departments & positions dengan data dummy saat aplikasi start,
 * HANYA jika tabel masih kosong.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("⏭️  Seed skipped — data already exists.");
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
                Position.builder().title("Software Engineer").department(engineering).build(),
                Position.builder().title("Senior Software Engineer").department(engineering).build(),
                Position.builder().title("Tech Lead").department(engineering).build(),
                Position.builder().title("QA Engineer").department(engineering).build(),

                // Human Resources
                Position.builder().title("HR Manager").department(hr).build(),
                Position.builder().title("HR Staff").department(hr).build(),
                Position.builder().title("Recruiter").department(hr).build(),

                // Finance
                Position.builder().title("Finance Manager").department(finance).build(),
                Position.builder().title("Accountant").department(finance).build(),
                Position.builder().title("Financial Analyst").department(finance).build(),

                // Marketing
                Position.builder().title("Marketing Manager").department(marketing).build(),
                Position.builder().title("Content Strategist").department(marketing).build(),
                Position.builder().title("Digital Marketer").department(marketing).build(),

                // Operations
                Position.builder().title("Operations Manager").department(operations).build(),
                Position.builder().title("Office Administrator").department(operations).build()
        ));

        log.info("✅ {} positions seeded.", positionRepository.count());
        log.info("🌱 Seeding complete!");
    }
}
