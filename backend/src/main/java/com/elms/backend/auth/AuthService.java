package com.elms.backend.auth;

import com.elms.backend.auth.dto.AuthUserResponse;
import com.elms.backend.auth.dto.LoginRequest;
import com.elms.backend.auth.dto.LoginResponse;
import com.elms.backend.auth.jwt.JwtTokenProvider;
import com.elms.backend.common.exception.ResourceNotFoundException;
import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        String token = jwtTokenProvider.generateToken(employee);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(AuthUserResponse.fromEntity(employee))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        return AuthUserResponse.fromEntity(employee);
    }
}
