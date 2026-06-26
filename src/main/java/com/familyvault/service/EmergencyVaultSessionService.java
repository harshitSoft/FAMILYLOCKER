package com.familyvault.service;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyMember;
import com.familyvault.exception.ApiException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EmergencyVaultSessionService {
    private static final long SESSION_TTL_SECONDS = 6 * 60 * 60;
    private final Map<String, EmergencySession> sessions = new ConcurrentHashMap<>();

    public String create(FamilyMember target) {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new EmergencySession(target.getId(), target.getFamily().getId(), Instant.now().plusSeconds(SESSION_TTL_SECONDS)));
        return id;
    }

    public void validate(String sessionId, Long targetMemberId, Family family) {
        EmergencySession session = requireSession(sessionId);
        if (!session.targetMemberId().equals(targetMemberId) || !session.familyId().equals(family.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Emergency session does not match this vault");
        }
    }

    public Long targetMemberId(String sessionId) {
        return requireSession(sessionId).targetMemberId();
    }

    private EmergencySession requireSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Emergency session is required");
        }
        EmergencySession session = sessions.get(sessionId);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId);
            throw new ApiException(HttpStatus.FORBIDDEN, "Emergency session is invalid or expired");
        }
        return session;
    }

    private record EmergencySession(Long targetMemberId, Long familyId, Instant expiresAt) {}
}
