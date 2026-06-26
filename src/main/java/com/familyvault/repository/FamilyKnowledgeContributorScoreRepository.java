package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyKnowledgeContributorScore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyKnowledgeContributorScoreRepository extends JpaRepository<FamilyKnowledgeContributorScore, Long> {
    Optional<FamilyKnowledgeContributorScore> findByFamilyAndContributorMemberId(Family family, Long contributorMemberId);
    Optional<FamilyKnowledgeContributorScore> findByFamilyAndContributorNameIgnoreCase(Family family, String contributorName);
    List<FamilyKnowledgeContributorScore> findByFamilyOrderByTotalPointsDescTotalContributionsDescUpdatedAtDesc(Family family);
}
