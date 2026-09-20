package com.elms.backend.attendance;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class AttendanceSpecification {

    private AttendanceSpecification() {}

    public static Specification<Attendance> withEmployeeId(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("employee").get("id"), employeeId);
        };
    }

    public static Specification<Attendance> withManagerId(UUID managerId) {
        return (root, query, cb) -> {
            if (managerId == null) return null;
            return cb.equal(root.get("employee").get("manager").get("id"), managerId);
        };
    }

    public static Specification<Attendance> withDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            return cb.equal(root.get("employee").get("department").get("id"), departmentId);
        };
    }

    public static Specification<Attendance> withStatus(AttendanceStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Attendance> withDateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) {
                return cb.between(root.get("date"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), from);
            }
            return cb.lessThanOrEqualTo(root.get("date"), to);
        };
    }
}
