package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);

    boolean existsByPrefix(String prefix);

    Optional<Category> findByPrefix(String prefix);
}
