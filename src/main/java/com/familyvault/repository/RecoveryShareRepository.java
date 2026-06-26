package com.familyvault.repository;

import com.familyvault.entity.FamilyMember;
import com.familyvault.entity.RecoveryShare;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoveryShareRepository extends JpaRepository<RecoveryShare, Long> {
    List<RecoveryShare> findByTargetMember(FamilyMember targetMember);
    boolean existsByTargetMemberIdAndHolderMemberId(Long targetMemberId, Long holderMemberId);
    Optional<RecoveryShare> findByTargetMemberIdAndHolderMemberId(Long targetMemberId, Long holderMemberId);
}
