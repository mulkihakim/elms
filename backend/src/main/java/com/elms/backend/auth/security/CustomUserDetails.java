package com.elms.backend.auth.security;

import com.elms.backend.employee.Employee;
import com.elms.backend.employee.EmploymentStatus;
import com.elms.backend.employee.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String fullName;
    private final Role role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Employee employee) {
        if (employee != null) {
            this.id = employee.getId();
            this.email = employee.getEmail();
            this.password = employee.getPassword();
            this.fullName = employee.getFullName();
            this.role = employee.getRole();
            this.active = employee.getEmploymentStatus() == EmploymentStatus.ACTIVE;
            this.authorities = employee.getRole() != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()))
                    : List.of();
        } else {
            this.id = null;
            this.email = null;
            this.password = null;
            this.fullName = null;
            this.role = null;
            this.active = false;
            this.authorities = List.of();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
