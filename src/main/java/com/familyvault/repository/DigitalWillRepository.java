package com.familyvault.repository;

import com.familyvault.entity.DigitalWill;
import com.familyvault.entity.FamilyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalWillRepository extends JpaRepository<DigitalWill, Long> {
    List<DigitalWill> findByOwnerMemberOrderByPriorityDescUpdatedAtDesc(FamilyMember ownerMember);
    List<DigitalWill> findByOwnerMemberAndVisibilityAfterEmergencyTrueOrderByPriorityDescUpdatedAtDesc(FamilyMember ownerMember);
    Optional<DigitalWill> findByIdAndOwnerMember(Long id, FamilyMember ownerMember);
}
