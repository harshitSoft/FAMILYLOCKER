package com.familyvault.service;

import com.familyvault.dto.ApiDtos.EmergencyRequestResponse;
import com.familyvault.dto.ApiDtos.FileResponse;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmergencyService {
    private final EmergencyAccessRequestRepository requests;
    private final EmergencyApprovalRepository approvals;
    private final FamilyMemberRepository members;
    private final LockerService lockerService;
    private final AuditService audit;
    private final int configuredThreshold;

    public EmergencyService(EmergencyAccessRequestRepository requests, EmergencyApprovalRepository approvals,
                            FamilyMemberRepository members, LockerService lockerService, AuditService audit,
                            @Value("${app.emergency.default-threshold}") int configuredThreshold) {
        this.requests = requests;
        this.approvals = approvals;
        this.members = members;
        this.lockerService = lockerService;
        this.audit = audit;
        this.configuredThreshold = configuredThreshold;
    }

    @Transactional
    public EmergencyRequestResponse request(Long targetMemberId, FamilyMember actor) {
        FamilyMember target = members.findById(targetMemberId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        ensureSameFamily(actor, target);
        if (actor.getId().equals(target.getId())) throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot request emergency access to your own locker");
        EmergencyAccessRequest request = new EmergencyAccessRequest();
        request.setTargetMember(target);
        request.setRequestedBy(actor);
        int remaining = Math.max(1, members.findByFamily(actor.getFamily()).size() - 1);
        request.setThreshold(Math.min(configuredThreshold, remaining));
        requests.save(request);
        approveInternal(request, actor);
        audit.record(AuditAction.EMERGENCY_REQUESTED, actor.getUser(), actor.getFamily(), target, "Emergency request created");
        return toResponse(request);
    }

    @Transactional
    public EmergencyRequestResponse approve(Long requestId, FamilyMember actor) {
        EmergencyAccessRequest request = getRequest(requestId);
        ensureSameFamily(actor, request.getTargetMember());
        if (actor.getId().equals(request.getTargetMember().getId())) throw new ApiException(HttpStatus.BAD_REQUEST, "Target member cannot approve");
        approveInternal(request, actor);
        audit.record(AuditAction.EMERGENCY_APPROVED, actor.getUser(), actor.getFamily(), request.getTargetMember(), "Emergency approval");
        if (!request.isUnlocked() && approvals.countByRequest(request) >= request.getThreshold()) {
            request.setUnlocked(true);
            request.setUnlockedAt(Instant.now());
            audit.record(AuditAction.EMERGENCY_UNLOCKED, actor.getUser(), actor.getFamily(), request.getTargetMember(), "Threshold reached");
        }
        return toResponse(request);
    }

    public List<FileResponse> unlockedFiles(Long requestId, FamilyMember actor) {
        EmergencyAccessRequest request = unlockedRequest(requestId, actor);
        return lockerService.emergencyVisibleFiles(request.getTargetMember());
    }

    public ResponseEntity<ByteArrayResource> download(Long requestId, Long fileId, FamilyMember actor) {
        EmergencyAccessRequest request = unlockedRequest(requestId, actor);
        return lockerService.download(request.getTargetMember(), fileId, true, actor.getUser());
    }

    private EmergencyAccessRequest unlockedRequest(Long requestId, FamilyMember actor) {
        EmergencyAccessRequest request = getRequest(requestId);
        ensureSameFamily(actor, request.getTargetMember());
        if (!request.isUnlocked()) throw new ApiException(HttpStatus.FORBIDDEN, "Emergency request is not unlocked");
        return request;
    }

    private EmergencyAccessRequest getRequest(Long id) {
        return requests.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Emergency request not found"));
    }

    private void approveInternal(EmergencyAccessRequest request, FamilyMember actor) {
        if (!approvals.existsByRequestAndApprover(request, actor)) {
            EmergencyApproval approval = new EmergencyApproval();
            approval.setRequest(request);
            approval.setApprover(actor);
            approvals.save(approval);
            request.getApprovals().add(approval);
        }
    }

    private void ensureSameFamily(FamilyMember actor, FamilyMember target) {
        if (!actor.getFamily().getId().equals(target.getFamily().getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
    }

    private EmergencyRequestResponse toResponse(EmergencyAccessRequest request) {
        long count = approvals.countByRequest(request);
        return new EmergencyRequestResponse(request.getId(), request.getTargetMember().getId(), request.getTargetMember().getFullName(),
                request.getThreshold(), count, request.isUnlocked(), request.getCreatedAt());
    }
}
