package com.elms.backend.auth;

import com.elms.backend.auth.dto.AuthUserResponse;
import com.elms.backend.auth.dto.LoginRequest;
import com.elms.backend.auth.dto.LoginResponse;
import com.elms.backend.common.exception.GlobalExceptionHandler;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private LoginResponse sampleLoginResponse;
    private AuthUserResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        UUID empId = UUID.randomUUID();
        sampleUserResponse = AuthUserResponse.builder()
                .id(empId)
                .email("budi.hr@elms.com")
                .fullName("Budi Santoso")
                .role(Role.HR)
                .departmentName("Human Resources")
                .positionTitle("HR Manager")
                .leaveBalance(12)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        sampleLoginResponse = LoginResponse.builder()
                .accessToken("mock-jwt-token-xyz")
                .tokenType("Bearer")
                .user(sampleUserResponse)
                .build();
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("budi.hr@elms.com", "password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(sampleLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token-xyz"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("budi.hr@elms.com"))
                .andExpect(jsonPath("$.data.user.role").value("HR"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_ValidationError_BlankFields() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void login_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("budi.hr@elms.com", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(sampleUserResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("budi.hr@elms.com"))
                .andExpect(jsonPath("$.data.fullName").value("Budi Santoso"))
                .andExpect(jsonPath("$.data.role").value("HR"));
    }
}
