package com.familyvault.repository;

import com.familyvault.entity.LegendLocker;
import com.familyvault.entity.LegendMemory;
import com.familyvault.entity.Family;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegendMemoryRepository extends JpaRepository<LegendMemory, Long> {
    List<LegendMemory> findByLegendLockerOrderByCreatedAtDesc(LegendLocker legendLocker);
    List<LegendMemory> findByLegendLockerFamilyOrderByCreatedAtDesc(Family family);
    Optional<LegendMemory> findByIdAndLegendLocker(Long id, LegendLocker legendLocker);
}
