package com.familyvault.service;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyMember;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmergencyPinService {
    private static final int MAX_ATTEMPTS = 1_000;
    private final FamilyMemberRepository members;
    private final SecureRandom random = new SecureRandom();

    public EmergencyPinService(FamilyMemberRepository members) {
        this.members = members;
    }

    @Transactional
    public void ensureEmergencyPinsForFamily(Family family) {
        if (family == null || family.getId() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to prepare emergency PINs. Please try again.");
        }
        List<FamilyMember> familyMembers = members.findByFamily(family);
        Set<String> usedPins = new HashSet<>();
        boolean changed = false;
        for (FamilyMember member : familyMembers) {
            String pin = member.getEmergencyPin();
            if (!isValidPin(pin) || !usedPins.add(pin)) {
                member.setEmergencyPin(nextUnusedPin(usedPins));
                usedPins.add(member.getEmergencyPin());
                changed = true;
            }
        }
        if (changed) {
            members.saveAll(familyMembers);
        }
    }

    public String generateUniquePin(Family family) {
        Set<String> usedPins = new HashSet<>();
        for (FamilyMember member : members.findByFamily(family)) {
            if (isValidPin(member.getEmergencyPin())) usedPins.add(member.getEmergencyPin());
        }
        return nextUnusedPin(usedPins);
    }

    private String nextUnusedPin(Set<String> usedPins) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = String.valueOf(1000 + random.nextInt(9000));
            if (!usedPins.contains(candidate)) return candidate;
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to prepare emergency PINs. Please try again.");
    }

    private boolean isValidPin(String pin) {
        return pin != null && pin.matches("[1-9]\\d{3}");
    }
}
