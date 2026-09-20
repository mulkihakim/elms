package com.elms.backend.performance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewPeriodRepository extends JpaRepository<ReviewPeriod, Long> {

    boolean existsByName(String name);

    List<ReviewPeriod> findAllByOrderByStartDateDesc();
}
