package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyKnowledgeEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyKnowledgeEntryRepository extends JpaRepository<FamilyKnowledgeEntry, Long> {
    List<FamilyKnowledgeEntry> findByFamilyOrderByCreatedAtDesc(Family family);
    Optional<FamilyKnowledgeEntry> findByIdAndFamily(Long id, Family family);
}
