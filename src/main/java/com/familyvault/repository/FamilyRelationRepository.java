package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyRelation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRelationRepository extends JpaRepository<FamilyRelation, Long> {
    List<FamilyRelation> findByFamilyOrderByIdAsc(Family family);
    Optional<FamilyRelation> findByIdAndFamily(Long id, Family family);
}
