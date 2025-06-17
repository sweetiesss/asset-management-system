package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.AssetReturn;
import com.nashtech.rookies.oam.model.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetReturnRepository extends JpaRepository<AssetReturn, UUID> {
    boolean existsByAssignment(Assignment assignment);
    Optional<AssetReturn> findTopByAssignmentOrderByCreatedAtDesc(Assignment assignment);

    @EntityGraph(attributePaths = {
            "assignment", "assignment.asset", "assignment.user"
    })
    Page<AssetReturn> findAll(Specification<AssetReturn> spec, Pageable pageable);
}
