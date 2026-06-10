package com.emergency.repository;

import com.emergency.model.DisasterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisasterTypeRepository extends JpaRepository<DisasterType, Long> {
    Optional<DisasterType> findBySlug(String slug);
    Optional<DisasterType> findByNameContainingIgnoreCase(String name);
    boolean existsBySlug(String slug);
    List<DisasterType> findAllByOrderByNameAsc();
}
