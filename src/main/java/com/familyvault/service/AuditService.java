package com.familyvault.service;

import com.familyvault.entity.*;
import com.familyvault.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository audits;

    public AuditService(AuditLogRepository audits) {
        this.audits = audits;
    }

    public void record(AuditAction action, AppUser actor, Family family, FamilyMember target, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorUserId(actor == null ? null : actor.getId());
        log.setFamilyId(family == null ? null : family.getId());
        log.setTargetMemberId(target == null ? null : target.getId());
        log.setDetails(details);
        audits.save(log);
    }
}
