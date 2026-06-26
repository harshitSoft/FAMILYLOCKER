package com.familyvault.service;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyMember;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.util.CryptoUtil;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EmergencyKeyGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(EmergencyKeyGeneratorService.class);
    private final FamilyMemberRepository members;
    private final CryptoUtil crypto;

    public EmergencyKeyGeneratorService(FamilyMemberRepository members, CryptoUtil crypto) {
        this.members = members;
        this.crypto = crypto;
    }

    public String expectedKeyFor(FamilyMember target) {
        if (target == null || target.getFamily() == null || target.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target member is required for emergency access.");
        }

        List<FamilyMember> orderedMembers = members.findByFamily(target.getFamily()).stream()
                .sorted(Comparator.comparing(FamilyMember::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FamilyMember::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<FamilyMember> contributors = orderedMembers.stream()
                .filter(member -> !member.getId().equals(target.getId()))
                .toList();
        if (contributors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Emergency access requires at least one other family member.");
        }

        String expectedKey = contributors.stream().map(this::secretPart).collect(Collectors.joining());
        log.debug("Emergency secret generated: targetMemberId={}, orderedMemberIds={}, contributorMemberIds={}, expectedLength={}",
                target.getId(),
                orderedMembers.stream().map(FamilyMember::getId).toList(),
                contributors.stream().map(FamilyMember::getId).toList(),
                expectedKey.length());
        return expectedKey;
    }

    private String secretPart(FamilyMember member) {
        if (member.getEncryptedEmergencySecretPart() == null || member.getEncryptedEmergencySecretPart().isBlank()) {
            log.debug("Emergency secret part missing for memberId={}", member.getId());
            throw missingSecretPart();
        }
        if (isBcryptHash(member.getEncryptedEmergencySecretPart())) {
            log.debug("Emergency secret part is a BCrypt hash for memberId={}", member.getId());
            throw missingSecretPart();
        }
        String value = crypto.decryptTextOrFallback(member.getEncryptedEmergencySecretPart());
        if (value == null || value.isBlank()) {
            log.debug("Emergency secret part could not be read for memberId={}", member.getId());
            throw missingSecretPart();
        }
        return value.trim();
    }

    private boolean isBcryptHash(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$");
    }

    private ApiException missingSecretPart() {
        return new ApiException(HttpStatus.BAD_REQUEST,
                "Emergency secret part is missing for one or more members. Re-save or reset that member's emergency secret part.");
    }
}
