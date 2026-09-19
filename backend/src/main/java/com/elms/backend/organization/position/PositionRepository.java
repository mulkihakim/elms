package com.elms.backend.organization.position;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByDepartmentId(Long departmentId);

    Page<Position> findByDepartmentId(Long departmentId, Pageable pageable);

    boolean existsByTitleAndDepartmentId(String title, Long departmentId);

    boolean existsByTitleAndDepartmentIdAndIdNot(String title, Long departmentId, Long id);
}
