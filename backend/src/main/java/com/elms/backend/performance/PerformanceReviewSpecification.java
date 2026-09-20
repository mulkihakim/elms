package com.elms.backend.performance;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PerformanceReviewSpecification {

    private PerformanceReviewSpecification() {}

    public static Specification<PerformanceReview> withPeriodId(Long periodId) {
        return (root, query, cb) -> {
            if (periodId == null) return null;
            return cb.equal(root.get("reviewPeriod").get("id"), periodId);
        };
    }

    public static Specification<PerformanceReview> withEmployeeId(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("employee").get("id"), employeeId);
        };
    }

    public static Specification<PerformanceReview> withReviewerId(UUID reviewerId) {
        return (root, query, cb) -> {
            if (reviewerId == null) return null;
            return cb.equal(root.get("reviewer").get("id"), reviewerId);
        };
    }

    public static Specification<PerformanceReview> withDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            return cb.equal(root.get("employee").get("department").get("id"), departmentId);
        };
    }
}
