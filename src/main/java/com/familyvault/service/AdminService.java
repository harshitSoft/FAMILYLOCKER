package com.familyvault.service;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import com.familyvault.util.CryptoUtil;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.crypto.SecretKey;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private static final List<String> DEFAULT_FOLDERS = List.of(
            "PRIVATE FOLDER", "OFFICIAL DOCUMENTS");
    private final FamilyRepository families;
    private final FamilyMemberRepository members;
    private final AppUserRepository users;
    private final RecoveryShareRepository recoveryShares;
    private final LockerRepository lockers;
    private final ActivityLogRepository activities;
    private final PasswordEncoder encoder;
    private final CryptoUtil crypto;
    private final EmergencyPinService emergencyPins;
    private final AuditService audit;
    private final SecureRandom random = new SecureRandom();

    public AdminService(FamilyRepository families, FamilyMemberRepository members, AppUserRepository users, RecoveryShareRepository recoveryShares,
                        LockerRepository lockers, ActivityLogRepository activities,
                        PasswordEncoder encoder, CryptoUtil crypto, AuditService audit, EmergencyPinService emergencyPins) {
        this.families = families;
        this.members = members;
        this.users = users;
        this.recoveryShares = recoveryShares;
        this.lockers = lockers;
        this.activities = activities;
        this.encoder = encoder;
        this.crypto = crypto;
        this.audit = audit;
        this.emergencyPins = emergencyPins;
    }

    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request, AppUser actor) {
        String familyName = request.resolvedName();
        if (familyName == null || familyName.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Family name is required");
        }
        if (request.familyCode() == null || request.familyCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Family code is required");
        }
        if (request.familyPassword() == null || request.familyPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Family password is required");
        }
        String familyCode = normalizeFamilyCode(request.familyCode() == null || request.familyCode().isBlank()
                ? uniqueFamilyCode(familyName)
                : request.familyCode());
        if (families.existsByFamilyCode(familyCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "Family code already exists. Please choose another family code.");
        }
        Family family = new Family();
        family.setName(familyName.trim());
        family.setFamilyCode(familyCode);
        if (request.familyPassword() == null || request.familyPassword().length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Family password must be at least 8 characters");
        }
        family.setFamilyPasswordHash(encoder.encode(request.familyPassword()));
        family.setActive(true);
        family.setBlocked(false);
        families.save(family);

        AppUser familyUser = new AppUser();
        familyUser.setUsername("FAMILY:" + family.getFamilyCode());
        familyUser.setPasswordHash(family.getFamilyPasswordHash());
        familyUser.setRole(Role.FAMILY_MEMBER);
        users.save(familyUser);
        audit.record(AuditAction.FAMILY_CREATED, actor, family, null, "Family created");
        logActivity("FAMILY_CREATED", family.getName() + " created", actor);
        return toFamily(family);
    }

    public List<FamilyResponse> listFamilies() {
        return families.findAllByOrderByCreatedAtDesc().stream().map(this::toFamily).toList();
    }

    public DashboardResponse dashboard() {
        return new DashboardResponse(
                families.count(),
                families.countByActiveTrueAndBlockedFalse(),
                families.countByActiveFalse() + families.countByActiveTrueAndBlockedTrue(),
                members.count()
        );
    }

    public List<ActivityLogResponse> recentActivity() {
        return activities.findTop10ByOrderByCreatedAtDesc().stream()
                .map(item -> new ActivityLogResponse(item.getId(), item.getAction(), item.getDescription(), item.getCreatedAt(), item.getPerformedBy()))
                .toList();
    }

    @Transactional
    public void deleteFamily(Long familyId, AppUser actor) {
        Family family = familyById(familyId);
        family.setActive(false);
        family.setBlocked(true);
        users.findByUsername("FAMILY:" + family.getFamilyCode()).ifPresent(user -> user.setEnabled(false));
        logActivity("FAMILY_DELETED", family.getName() + " deleted", actor);
    }

    @Transactional
    public FamilyResponse disableFamily(Long familyId, AppUser actor) {
        Family family = familyById(familyId);
        family.setActive(false);
        family.setBlocked(true);
        users.findByUsername("FAMILY:" + family.getFamilyCode()).ifPresent(user -> user.setEnabled(false));
        logActivity("FAMILY_DISABLED", family.getName() + " disabled", actor);
        return toFamily(family);
    }

    @Transactional
    public FamilyResponse enableFamily(Long familyId, AppUser actor) {
        Family family = familyById(familyId);
        family.setActive(true);
        family.setBlocked(false);
        users.findByUsername("FAMILY:" + family.getFamilyCode()).ifPresent(user -> user.setEnabled(true));
        logActivity("FAMILY_ENABLED", family.getName() + " enabled", actor);
        return toFamily(family);
    }

    @Transactional
    public FamilyResponse blockFamily(Long familyId, AppUser actor) {
        return disableFamily(familyId, actor);
    }

    @Transactional
    public FamilyResponse unblockFamily(Long familyId, AppUser actor) {
        return enableFamily(familyId, actor);
    }

    public List<MemberSummary> memberSummary(String familyCode) {
        Family family = families.findByFamilyCode(familyCode).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Family not found"));
        return members.findByFamily(family).stream()
                .map(m -> new MemberSummary(m.getId(), m.getMemberCode(), m.getFullName(), m.getRelationship(), m.getProfilePhotoPath(), false))
                .toList();
    }

    @Transactional
    public FamilyMember createMember(Family family, MemberSeed seed) {
        String memberId = normalizeMemberId(seed.resolvedMemberId());
        if (memberId == null || memberId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Member username is required");
        }
        if (members.findByFamilyAndMemberCode(family, memberId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Duplicate member ID in family");
        }
        AppUser user = new AppUser();
        user.setUsername(family.getFamilyCode() + ":" + memberId);
        String password = seed.resolvedPassword() == null || seed.resolvedPassword().isBlank() ? "ChangeMe@123" : seed.resolvedPassword();
        if (password.length() < 8) throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        user.setPasswordHash(encoder.encode(password));
        user.setRole(Role.FAMILY_MEMBER);
        users.save(user);

        FamilyMember member = new FamilyMember();
        member.setFamily(family);
        member.setMemberCode(memberId);
        member.setFullName(seed.fullName());
        member.setRelationship(seed.relationship());
        member.setUser(user);
        member.setEmergencyPin(emergencyPins.generateUniquePin(family));
        member.setEncryptedEmergencySecretPart(crypto.encryptText(password));
        members.save(member);

        Locker locker = new Locker();
        locker.setOwner(member);
        SecretKey vaultKey = crypto.generateVaultKey();
        locker.setEncryptedVaultKey(crypto.encryptVaultKey(vaultKey));
        member.setLocker(locker);
        for (String folderName : DEFAULT_FOLDERS) {
            Folder folder = new Folder();
            folder.setName(folderName);
            folder.setDefaultFolder(true);
            folder.setHidden("PRIVATE FOLDER".equals(folderName));
            folder.setLocker(locker);
            locker.getFolders().add(folder);
        }
        FamilyMember saved = members.save(member);
        regenerateRecoveryShares(family);
        return saved;
    }

    private FamilyResponse toFamily(Family family) {
        boolean enabled = family.isActive() && !family.isBlocked();
        String status = enabled ? "ACTIVE" : "DISABLED";
        int memberCount = Math.toIntExact(members.countByFamilyId(family.getId()));
        return new FamilyResponse(family.getId(), family.getName(), family.getFamilyCode(), enabled, status,
                memberCount, family.getCreatedAt());
    }

    private Family familyById(Long id) {
        return families.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Family not found"));
    }

    private void logActivity(String action, String description, AppUser actor) {
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setDescription(description);
        log.setPerformedBy(actor == null ? "system" : actor.getUsername());
        activities.save(log);
    }

    private void regenerateRecoveryShares(Family family) {
        List<FamilyMember> familyMembers = members.findByFamily(family);
        for (FamilyMember target : familyMembers) {
            for (FamilyMember holder : familyMembers) {
                if (target.getId().equals(holder.getId())) continue;
                upsertRecoveryShare(target, holder);
            }
        }
    }

    private void upsertRecoveryShare(FamilyMember target, FamilyMember holder) {
        String logicalShare = "RECOVERY_SHARE:" + target.getId() + ":" + holder.getId();
        RecoveryShare share = recoveryShares.findByTargetMemberIdAndHolderMemberId(target.getId(), holder.getId())
                .orElseGet(() -> {
                    RecoveryShare next = new RecoveryShare();
                    next.setTargetMember(target);
                    next.setHolderMember(holder);
                    return next;
                });
        share.setEncryptedShare(crypto.encryptText(logicalShare));
        recoveryShares.save(share);
    }

    private String uniqueFamilyCode(String name) {
        String prefix = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        prefix = (prefix.length() < 3 ? "FAM" : prefix.substring(0, Math.min(4, prefix.length())));
        for (int i = 0; i < 100; i++) {
            byte[] bytes = new byte[3];
            random.nextBytes(bytes);
            String code = prefix + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase(Locale.ROOT);
            if (!families.existsByFamilyCode(code)) return code;
        }
        throw new ApiException(HttpStatus.CONFLICT, "Unable to generate family ID");
    }

    public String suggestFamilyCode(String name) {
        return uniqueFamilyCode(name == null || name.isBlank() ? "Family" : name);
    }

    private String normalizeFamilyCode(String value) {
        return value.trim().replaceAll("\\s+", "-").replaceAll("[^A-Za-z0-9-]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeMemberId(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", "-");
    }
}
