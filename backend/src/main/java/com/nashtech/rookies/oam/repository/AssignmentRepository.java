package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.model.Assignment;
import com.nashtech.rookies.oam.projection.AssignmentEditViewProjection;
import com.nashtech.rookies.oam.projection.AssignmentWithReturnDate;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {
    @Query("""
            SELECT a AS assignment, ar.returnedDate AS returnedDate
            FROM assignments a
            JOIN FETCH a.user
            LEFT JOIN asset_returns ar ON ar.assignment = a
            WHERE a.asset.id = :assetId
                 AND a.status.name IN :statuses
            """)
    Page<AssignmentWithReturnDate> findAllWithReturnDateByAsset_Id(@Param("assetId") UUID assetId, List<String> statuses, Pageable pageable);

    Boolean existsByAsset(Asset asset);

    Optional<AssignmentEditViewProjection> findProjectedById(UUID id);

    @EntityGraph(attributePaths = {"asset", "user", "status", "asset.category"})
    Page<Assignment> findAll(Specification<Assignment> spec, Pageable pageable);
    
    @EntityGraph(attributePaths = {"asset", "user", "status"})
    Optional<Assignment> findWithAssetAndUserById(UUID id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("""
            SELECT a
            FROM assignments a
            WHERE a.id = :id
            """)
    Optional<Assignment> getAssignmentByIdForDelete(UUID id);

    @Query("""
    SELECT COUNT(a) > 0 FROM assignments a
    WHERE a.user.id = :userId AND a.status.name IN :statuses
    """)
    boolean hasAssignmentsWithStatuses(UUID userId, List<String> statuses);
}
