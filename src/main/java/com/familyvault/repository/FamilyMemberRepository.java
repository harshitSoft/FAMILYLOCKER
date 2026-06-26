package com.familyvault.repository;

import com.familyvault.entity.AppUser;
import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    @EntityGraph(attributePaths = {"user", "family", "locker"})
    Optional<FamilyMember> findByUser(AppUser user);
    @EntityGraph(attributePaths = {"user", "family", "locker"})
    Optional<FamilyMember> findByFamilyAndMemberCode(Family family, String memberCode);
    @EntityGraph(attributePaths = {"user", "family", "locker"})
    Optional<FamilyMember> findByIdAndFamily(Long id, Family family);
    @EntityGraph(attributePaths = {"user", "family", "locker"})
    List<FamilyMember> findByFamily(Family family);
    long countByFamilyId(Long familyId);
}
