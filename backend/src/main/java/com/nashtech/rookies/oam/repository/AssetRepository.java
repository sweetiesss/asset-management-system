package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.Asset;
import com.nashtech.rookies.oam.projection.EditAssetProjection;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    @Lock(LockModeType.OPTIMISTIC)
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT a FROM assets a WHERE a.id = :id")
    Optional<Asset> findByIdForUpdate(UUID id);

    Optional<EditAssetProjection> findProjectedById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "location"})
    @NonNull
    Optional<Asset> findById(@NonNull UUID uuid);

    @EntityGraph(attributePaths = {"category"})
    Page<Asset> findAll(Specification<Asset> spec, Pageable pageable);



}
