package com.elms.backend.performance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID>, JpaSpecificationExecutor<PerformanceReview> {

    boolean existsByReviewPeriodIdAndEmployeeId(Long reviewPeriodId, UUID employeeId);

    Optional<PerformanceReview> findByReviewPeriodIdAndEmployeeId(Long reviewPeriodId, UUID employeeId);

    Optional<PerformanceReview> findFirstByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}
