package com.elms.backend.employee;

import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> withDepartment(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    public static Specification<Employee> withEmploymentStatus(EmploymentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("employmentStatus"), status);
        };
    }

    public static Specification<Employee> withRole(Role role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role"), role);
        };
    }

    public static Specification<Employee> withSearch(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }
}
