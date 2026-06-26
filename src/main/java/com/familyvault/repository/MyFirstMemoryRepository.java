package com.familyvault.repository;

import com.familyvault.entity.FamilyMember;
import com.familyvault.entity.MyFirstMemory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyFirstMemoryRepository extends JpaRepository<MyFirstMemory, Long> {
    List<MyFirstMemory> findByOwnerMemberOrderByEventDateDescCreatedAtDesc(FamilyMember ownerMember);
    List<MyFirstMemory> findByOwnerMemberAndVisibleAfterEmergencyTrueOrderByEventDateDescCreatedAtDesc(FamilyMember ownerMember);
    Optional<MyFirstMemory> findByIdAndOwnerMember(Long id, FamilyMember ownerMember);
}
