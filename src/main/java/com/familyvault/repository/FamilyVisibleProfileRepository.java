package com.familyvault.repository;

import com.familyvault.entity.FamilyVisibleProfile;
import com.familyvault.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyVisibleProfileRepository extends JpaRepository<FamilyVisibleProfile, Long> {
    java.util.Optional<FamilyVisibleProfile> findByMember(FamilyMember member);
}
