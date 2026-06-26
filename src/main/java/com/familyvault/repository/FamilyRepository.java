package com.familyvault.repository;

import com.familyvault.entity.Family;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByFamilyCode(String familyCode);
    boolean existsByFamilyCode(String familyCode);
    List<Family> findByActiveTrueOrderByCreatedAtDesc();
    List<Family> findAllByOrderByCreatedAtDesc();
    long countByActiveTrue();
    long countByActiveFalse();
    long countByActiveTrueAndBlockedFalse();
    long countByActiveTrueAndBlockedTrue();
}
