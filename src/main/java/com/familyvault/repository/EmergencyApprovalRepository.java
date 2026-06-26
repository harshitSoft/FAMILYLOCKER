package com.familyvault.repository;

import com.familyvault.entity.EmergencyAccessRequest;
import com.familyvault.entity.EmergencyApproval;
import com.familyvault.entity.FamilyMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyApprovalRepository extends JpaRepository<EmergencyApproval, Long> {
    long countByRequest(EmergencyAccessRequest request);
    boolean existsByRequestAndApprover(EmergencyAccessRequest request, FamilyMember approver);
    Optional<EmergencyApproval> findByRequestAndApprover(EmergencyAccessRequest request, FamilyMember approver);
}
