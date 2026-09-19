package com.elms.backend.auth;

import com.elms.backend.auth.jwt.JwtTokenProvider;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Employee sampleEmployee;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "my_very_secure_test_secret_key_that_is_long_enough_12345");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L); // 1 hour
        jwtTokenProvider.init();

        employeeId = UUID.randomUUID();
        sampleEmployee = Employee.builder()
                .id(employeeId)
                .email("budi.hr@elms.com")
                .fullName("Budi Santoso")
                .role(Role.HR)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    @Test
    void generateAndValidateToken_Success() {
        String token = jwtTokenProvider.generateToken(sampleEmployee);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("budi.hr@elms.com", jwtTokenProvider.getEmailFromToken(token));
        assertEquals(Role.HR, jwtTokenProvider.getRoleFromToken(token));
        assertEquals(employeeId, jwtTokenProvider.getEmployeeIdFromToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }
}
