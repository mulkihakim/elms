package com.elms.backend.leave;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class LeaveSpecification {

    private LeaveSpecification() {}

    public static Specification<LeaveRequest> withEmployeeId(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("employee").get("id"), employeeId);
        };
    }

    public static Specification<LeaveRequest> withManagerId(UUID managerId) {
        return (root, query, cb) -> {
            if (managerId == null) return null;
            return cb.equal(root.get("employee").get("manager").get("id"), managerId);
        };
    }

    public static Specification<LeaveRequest> withDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            return cb.equal(root.get("employee").get("department").get("id"), departmentId);
        };
    }

    public static Specification<LeaveRequest> withStatus(LeaveStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<LeaveRequest> withDateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) {
                // Rentang cuti bersinggungan dengan [from, to]
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("startDate"), to),
                        cb.greaterThanOrEqualTo(root.get("endDate"), from)
                );
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("endDate"), from);
            }
            return cb.lessThanOrEqualTo(root.get("startDate"), to);
        };
    }
}
