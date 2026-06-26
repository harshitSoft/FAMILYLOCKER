package com.familyvault.repository;

import com.familyvault.entity.FamilyMember;
import com.familyvault.entity.LegacyMessage;
import com.familyvault.entity.LegacyReleaseType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegacyMessageRepository extends JpaRepository<LegacyMessage, Long> {
    List<LegacyMessage> findByOwnerMemberOrderByPriorityDescUpdatedAtDesc(FamilyMember ownerMember);
    List<LegacyMessage> findByOwnerMemberAndReleaseTypeOrderByPriorityDescUpdatedAtDesc(FamilyMember ownerMember, LegacyReleaseType releaseType);
    Optional<LegacyMessage> findByIdAndOwnerMember(Long id, FamilyMember ownerMember);
}
