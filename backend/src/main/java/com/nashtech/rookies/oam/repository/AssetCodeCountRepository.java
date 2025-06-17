package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.AssetCodeCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetCodeCountRepository extends JpaRepository<AssetCodeCount, String> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM asset_code_count s WHERE s.id = :id")
    Optional<AssetCodeCount> findByIdForUpdate(String id);
}
