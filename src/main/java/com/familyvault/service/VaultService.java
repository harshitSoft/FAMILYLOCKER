package com.familyvault.service;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import com.familyvault.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VaultService {
    private static final Logger log = LoggerFactory.getLogger(VaultService.class);
    private final FamilyMemberRepository members;
    private final LockerService lockerService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwt;
    private final EmergencyPinService emergencyPins;
    private final EmergencyAccessLogRepository emergencyLogs;
    private final EmergencyVaultSessionService emergencySessions;
    private final PasswordEncoder passwordEncoder;

    public VaultService(FamilyMemberRepository members, LockerService lockerService,
                        AuthenticationManager authenticationManager, JwtUtil jwt,
                        EmergencyPinService emergencyPins, EmergencyAccessLogRepository emergencyLogs,
                        EmergencyVaultSessionService emergencySessions,
                        PasswordEncoder passwordEncoder) {
        this.members = members;
        this.lockerService = lockerService;
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
        this.emergencyPins = emergencyPins;
        this.emergencyLogs = emergencyLogs;
        this.emergencySessions = emergencySessions;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public VaultOpenResponse open(VaultOpenRequest request, Family currentFamily) {
        FamilyMember target = resolveTarget(request, currentFamily);
        if (!target.getMemberCode().equals(request.resolvedUsername())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Username must belong to selected vault owner");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(target.getUser().getUsername(), request.resolvedPassword()));
        return new VaultOpenResponse(UUID.randomUUID().toString(), "NORMAL", jwt.issue(target.getUser()),
                target.getId(), target.getMemberCode(), target.getFullName(),
                target.getFamily().getFamilyCode(), target.getFamily().getName());
    }

    @Transactional
    public VaultProfileResponse profile(FamilyMember current) {
        emergencyPins.ensureEmergencyPinsForFamily(current.getFamily());
        FamilyMember member = members.findById(current.getId()).orElse(current);
        return new VaultProfileResponse(member.getId(), member.getFullName(), member.getMemberCode(),
                member.getRelationship(), member.getCreatedAt(), member.getFamily().getFamilyCode(),
                member.getFamily().getName(), null, member.getEmergencyPin());
    }

    @Transactional
    public EmergencyVaultProfileResponse emergencyProfile(String emergencySessionId, Family currentFamily) {
        FamilyMember target = emergencyTarget(emergencySessionId, currentFamily);
        emergencyPins.ensureEmergencyPinsForFamily(target.getFamily());
        FamilyMember refreshed = members.findById(target.getId()).orElse(target);
        return new EmergencyVaultProfileResponse(refreshed.getId(), refreshed.getFullName(),
                refreshed.getMemberCode(), true, refreshed.getEmergencyPin());
    }

    @Transactional
    public SimpleResponse changePassword(ChangeVaultPasswordRequest request, FamilyMember current) {
        if (request == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Password details are required");
        validateNewPassword(request.newPassword(), request.confirmPassword());
        if (!passwordEncoder.matches(request.oldPassword(), current.getUser().getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }
        updateLoginPasswordOnly(current, request.newPassword());
        return new SimpleResponse(true, "Password changed successfully.");
    }

    @Transactional
    public SimpleResponse emergencyChangePassword(String emergencySessionId,
                                                  EmergencyChangeVaultPasswordRequest request,
                                                  Family currentFamily) {
        if (request == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Password details are required");
        validateNewPassword(request.newPassword(), request.confirmPassword());
        FamilyMember target = emergencyTarget(emergencySessionId, currentFamily);
        updateLoginPasswordOnly(target, request.newPassword());
        return new SimpleResponse(true, "Vault password reset successfully.");
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "New password is required");
        if (newPassword.length() < 8) throw new ApiException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters");
        if (!newPassword.equals(confirmPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password and confirm password must match");
        }
    }

    private void updateLoginPasswordOnly(FamilyMember member, String newPassword) {
        member.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
        members.save(member);
    }

    private FamilyMember emergencyTarget(String emergencySessionId, Family currentFamily) {
        Long targetMemberId = emergencySessions.targetMemberId(emergencySessionId);
        FamilyMember target = members.findById(targetMemberId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        emergencySessions.validate(emergencySessionId, target.getId(), currentFamily);
        return target;
    }

    @Transactional
    public EmergencyUnlockResponse emergencyUnlock(Long targetMemberId, EmergencyUnlockRequest request, HttpServletRequest servletRequest) {
        FamilyMember target = members.findById(targetMemberId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        emergencyPins.ensureEmergencyPinsForFamily(target.getFamily());
        String supplied = request == null ? "" : request.resolvedSecretKey();
        boolean success = validEmergencyPinCombination(target, supplied);
        logEmergency(null, target, success, servletRequest);
        if (!success) throw new ApiException(HttpStatus.FORBIDDEN, "Invalid emergency PIN combination.");
        String sessionId = emergencySessions.create(target);
        return new EmergencyUnlockResponse(true, "EMERGENCY", sessionId, target.getId(),
                target.getId(), target.getMemberCode(), target.getFullName(),
                target.getFamily().getFamilyCode(), target.getFamily().getName(),
                "Emergency vault unlocked successfully");
    }

    private boolean validEmergencyPinCombination(FamilyMember target, String supplied) {
        String entered = supplied == null ? "" : supplied.replaceAll("\\s+", "").trim();
        List<FamilyMember> contributors = members.findByFamily(target.getFamily()).stream()
                .filter(member -> !member.getId().equals(target.getId()))
                .toList();
        if (contributors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Emergency access requires at least one other family member.");
        }
        if (!entered.matches("\\d+") || entered.length() % 4 != 0) {
            return false;
        }
        List<String> enteredPins = java.util.stream.IntStream.range(0, entered.length() / 4)
                .mapToObj(index -> entered.substring(index * 4, (index + 1) * 4))
                .toList();
        Set<String> requiredPins = contributors.stream().map(FamilyMember::getEmergencyPin).collect(java.util.stream.Collectors.toSet());
        Set<String> suppliedPins = new HashSet<>(enteredPins);
        boolean matches = enteredPins.size() == contributors.size()
                && suppliedPins.size() == enteredPins.size()
                && requiredPins.size() == contributors.size()
                && requiredPins.equals(suppliedPins);
        log.debug("Emergency PIN comparison: targetMemberId={}, requiredCount={}, enteredCount={}, uniqueEnteredCount={}, matches={}",
                target.getId(), contributors.size(), enteredPins.size(), suppliedPins.size(), matches);
        return matches;
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> folders(Long memberId, FamilyMember current, boolean emergency) {
        FamilyMember target = targetForAccess(memberId, current, emergency);
        return emergency ? lockerService.emergencyVisibleFolders(target) : lockerService.folders(target);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> emergencyFolders(Long memberId, Family currentFamily) {
        FamilyMember target = targetForEmergencyAccess(memberId, currentFamily);
        return lockerService.emergencyVisibleFolders(target);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> files(Long memberId, FamilyMember current, boolean emergency) {
        FamilyMember target = targetForAccess(memberId, current, emergency);
        return emergency ? lockerService.emergencyVisibleFiles(target) : lockerService.list(target);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> emergencyFiles(Long memberId, Family currentFamily) {
        FamilyMember target = targetForEmergencyAccess(memberId, currentFamily);
        return lockerService.emergencyVisibleFiles(target);
    }

    public FolderResponse createFolder(FolderRequest request, FamilyMember current) {
        return lockerService.createFolder(current, request);
    }

    public FolderResponse renameFolder(Long id, FolderRequest request, FamilyMember current) {
        return lockerService.updateFolder(current, id, request);
    }

    public void deleteFolder(Long id, FamilyMember current) {
        lockerService.deleteFolder(current, id);
    }

    public FileResponse upload(Long folderId, MultipartFile file, boolean hidden, FamilyMember current) {
        return lockerService.upload(current, folderId, file, hidden);
    }

    public void deleteFile(Long id, FamilyMember current) {
        lockerService.deleteFile(current, id);
    }

    public ResponseEntity<ByteArrayResource> download(Long memberId, Long fileId, FamilyMember current, boolean emergency) {
        FamilyMember target = targetForAccess(memberId, current, emergency);
        return lockerService.download(target, fileId, emergency, current.getUser());
    }

    public ResponseEntity<ByteArrayResource> emergencyDownload(Long memberId, Long fileId, AppUser currentUser, Family currentFamily) {
        FamilyMember target = targetForEmergencyAccess(memberId, currentFamily);
        return lockerService.download(target, fileId, true, currentUser);
    }


    private FamilyMember resolveTarget(VaultOpenRequest request, Family currentFamily) {
        FamilyMember target = request.targetMemberId() == null
                ? members.findByFamilyAndMemberCode(currentFamily, request.resolvedUsername()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"))
                : members.findById(request.targetMemberId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        ensureSameFamily(currentFamily, target);
        if (request.familyCode() != null && !request.familyCode().isBlank() && !currentFamily.getFamilyCode().equals(request.familyCode())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Target member does not belong to family code");
        }
        return target;
    }

    private FamilyMember targetForAccess(Long memberId, FamilyMember current, boolean emergency) {
        FamilyMember target = members.findById(memberId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        ensureSameFamily(current, target);
        if (!emergency && !target.getId().equals(current.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Normal vault access requires owner authentication");
        }
        return target;
    }

    private FamilyMember targetForEmergencyAccess(Long memberId, Family currentFamily) {
        FamilyMember target = members.findById(memberId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        ensureSameFamily(currentFamily, target);
        return target;
    }

    private void ensureSameFamily(FamilyMember a, FamilyMember b) {
        if (!a.getFamily().getId().equals(b.getFamily().getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
    }

    private void ensureSameFamily(Family family, FamilyMember member) {
        if (!family.getId().equals(member.getFamily().getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
    }

    private void logEmergency(FamilyMember requester, FamilyMember target, boolean success, HttpServletRequest request) {
        EmergencyAccessLog log = new EmergencyAccessLog();
        log.setRequestedBy(requester);
        log.setTargetMember(target);
        log.setSuccess(success);
        log.setIpAddress(request.getRemoteAddr());
        emergencyLogs.save(log);
    }
}

