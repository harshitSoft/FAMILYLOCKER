package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.LegendLocker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegendLockerRepository extends JpaRepository<LegendLocker, Long> {
    List<LegendLocker> findByFamilyOrderByCreatedAtDesc(Family family);
    Optional<LegendLocker> findByIdAndFamily(Long id, Family family);
}
