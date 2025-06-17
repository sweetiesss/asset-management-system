package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentStatusRepository extends JpaRepository<AssignmentStatus, Integer> {
    Optional<AssignmentStatus> findByName(String name);

}
