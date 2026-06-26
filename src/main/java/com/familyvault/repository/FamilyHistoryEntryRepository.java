package com.familyvault.repository;

import com.familyvault.entity.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyHistoryEntryRepository extends JpaRepository<FamilyHistoryEntry, Long> {
    List<FamilyHistoryEntry> findByFamilyOrderByCreatedAtDesc(Family family);
    List<FamilyHistoryEntry> findByFamilyAndSectionTypeOrderByCreatedAtDesc(Family family, FamilyHistorySectionType sectionType);
    Optional<FamilyHistoryEntry> findByIdAndFamilyAndSectionType(Long id, Family family, FamilyHistorySectionType sectionType);
}
