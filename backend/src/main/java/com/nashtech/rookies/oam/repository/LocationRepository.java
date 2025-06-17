package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByCode(String locationCode);
}
