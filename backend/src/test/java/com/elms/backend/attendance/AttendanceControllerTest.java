package com.elms.backend.attendance;

import com.elms.backend.attendance.dto.AttendanceResponse;
import com.elms.backend.attendance.dto.AttendanceSummaryResponse;
import com.elms.backend.attendance.dto.CheckInRequest;
import com.elms.backend.attendance.dto.TodayAttendanceResponse;
import com.elms.backend.auth.security.CustomUserDetails;
import com.elms.backend.common.exception.GlobalExceptionHandler;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@WebMvcTest(controllers = {AttendanceController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(AttendanceControllerTest.TestConfig.class)
class AttendanceControllerTest {

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    private UUID employeeId;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        Employee employee = Employee.builder()
                .id(employeeId)
                .fullName("Ahmad Fauzi")
                .email("ahmad.emp@elms.com")
                .role(Role.EMPLOYEE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        userDetails = new CustomUserDetails(employee);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void checkIn_Success() throws Exception {
        CheckInRequest request = new CheckInRequest("Working from office");
        AttendanceResponse response = AttendanceResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .employeeName("Ahmad Fauzi")
                .date(LocalDate.now())
                .checkIn(LocalDateTime.now())
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(attendanceService.checkIn(eq(employeeId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeName").value("Ahmad Fauzi"))
                .andExpect(jsonPath("$.data.status").value("ON_TIME"))
                .andExpect(jsonPath("$.message").value("Check-in successful"));
    }

    @Test
    void checkOut_Success() throws Exception {
        AttendanceResponse response = AttendanceResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .employeeName("Ahmad Fauzi")
                .date(LocalDate.now())
                .checkIn(LocalDateTime.now().minusHours(8))
                .checkOut(LocalDateTime.now())
                .status(AttendanceStatus.ON_TIME)
                .workMinutes(480)
                .build();

        when(attendanceService.checkOut(employeeId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workMinutes").value(480))
                .andExpect(jsonPath("$.message").value("Check-out successful"));
    }

    @Test
    void getTodayStatus_Success() throws Exception {
        TodayAttendanceResponse response = TodayAttendanceResponse.builder()
                .date(LocalDate.now())
                .checkedIn(true)
                .checkedOut(false)
                .checkIn(LocalDateTime.now())
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(attendanceService.getTodayStatus(employeeId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/today")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.checkedIn").value(true))
                .andExpect(jsonPath("$.data.checkedOut").value(false));
    }

    @Test
    void getTodaySummary_Success() throws Exception {
        AttendanceSummaryResponse summary = AttendanceSummaryResponse.builder()
                .date(LocalDate.now())
                .totalEmployees(20)
                .present(18)
                .onTime(15)
                .late(3)
                .absent(2)
                .build();

        when(attendanceService.getTodaySummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/attendance/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmployees").value(20))
                .andExpect(jsonPath("$.data.present").value(18))
                .andExpect(jsonPath("$.data.onTime").value(15));
    }
}
