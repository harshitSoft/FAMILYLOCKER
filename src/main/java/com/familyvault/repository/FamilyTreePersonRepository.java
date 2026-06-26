package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyTreePerson;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyTreePersonRepository extends JpaRepository<FamilyTreePerson, Long> {
    List<FamilyTreePerson> findByFamilyOrderByGenerationLevelAscSiblingOrderAscIdAsc(Family family);
    Optional<FamilyTreePerson> findByIdAndFamily(Long id, Family family);
    long countByFamily(Family family);
    List<FamilyTreePerson> findByFamilyAndNameContainingIgnoreCaseOrderByGenerationLevelAsc(Family family, String name);
    long countByFamilyAndGenerationLevel(Family family, int generationLevel);
}
