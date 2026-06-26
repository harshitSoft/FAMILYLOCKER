package com.familyvault.repository;

import com.familyvault.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyTreeRelationRepository extends JpaRepository<FamilyTreeRelation, Long> {
    List<FamilyTreeRelation> findByFamilyOrderByIdAsc(Family family);
    List<FamilyTreeRelation> findByFamilyAndFromPersonOrFamilyAndToPerson(Family familyA, FamilyTreePerson fromPerson, Family familyB, FamilyTreePerson toPerson);
    void deleteByFamilyAndFromPersonOrFamilyAndToPerson(Family familyA, FamilyTreePerson fromPerson, Family familyB, FamilyTreePerson toPerson);
    long countByFamilyAndFromPersonAndRelationType(Family family, FamilyTreePerson fromPerson, FamilyTreeRelationType relationType);
}
