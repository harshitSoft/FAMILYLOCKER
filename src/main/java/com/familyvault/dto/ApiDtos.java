package com.familyvault.dto;

import com.familyvault.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public class ApiDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record FamilyLoginRequest(@NotBlank String familyCode, @NotBlank String familyPassword) {}
    public record MemberLoginRequest(String familyCode, String familyId, String username, String memberId, @NotBlank String password) {
        public String resolvedFamilyCode() { return familyCode != null && !familyCode.isBlank() ? familyCode : familyId; }
        public String resolvedMemberId() { return username != null && !username.isBlank() ? username : memberId; }
    }
    public record MemberRegisterRequest(String familyCode, String familyId, String username, String memberId, @NotBlank @Size(min = 8) String password) {
        public String resolvedFamilyCode() { return familyCode != null && !familyCode.isBlank() ? familyCode : familyId; }
        public String resolvedMemberId() { return username != null && !username.isBlank() ? username : memberId; }
    }
    public record AuthResponse(String token, MeResponse user) {}
    public record MeResponse(Long userId, Role role, Long memberId, String familyCode, String memberCode, String fullName) {}

    public record CreateFamilyRequest(String name, String familyName, String familyCode, @Size(min = 8) String familyPassword,
                                      List<MemberSeed> members,
                                      String ownerFullName, String ownerRelationship, String ownerUsername,
                                      @Size(min = 8) String ownerPassword) {
        public String resolvedName() {
            return familyName != null && !familyName.isBlank() ? familyName : name;
        }
    }
    public record MemberSeed(String memberId, String username, @NotBlank String fullName, String relationship, String temporaryPassword, String password) {
        public String resolvedMemberId() { return username != null && !username.isBlank() ? username : memberId; }
        public String resolvedPassword() { return password != null && !password.isBlank() ? password : temporaryPassword; }
    }
    public record AddMemberRequest(@NotBlank String fullName, @NotBlank String relationship, String username, String memberId,
                                   @NotBlank @Size(min = 8) String password, String dateOfBirth, String phone, String email) {
        public String resolvedMemberId() { return username != null && !username.isBlank() ? username : memberId; }
    }
    public record FamilyResponse(Long id, String familyName, String familyCode, boolean active, String status,
                                 int memberCount, Instant createdAt) {}
    public record DashboardResponse(long totalFamilies, long activeFamilies, long disabledFamilies, long totalMembers) {}
    public record ActivityLogResponse(Long id, String action, String description, Instant createdAt, String performedBy) {}
    public record MemberSummary(Long id, String memberId, String fullName, String relationship, String profilePhotoUrl, boolean currentUser) {}

    public record TreeResponse(String familyCode, String familyName, List<MemberSummary> members) {}

    public record FolderRequest(@NotBlank String name, boolean hidden) {}
    public record FolderResponse(Long id, String name, boolean defaultFolder, boolean hidden, int fileCount, Instant createdAt) {}
    public record FileUpdateRequest(String name, Boolean hidden) {}
    public record FileResponse(Long id, Long folderId, String folderName, String originalName, String contentType, long sizeBytes, boolean hidden, Instant createdAt) {}
    public record EmergencyRequestResponse(Long id, Long targetMemberId, String targetMemberName, int threshold, long approvals, boolean unlocked, Instant createdAt) {}
    public record VaultOpenRequest(String familyCode, Long targetMemberId, String username, String memberId, String memberUsername, String password, String vaultPassword) {
        public String resolvedUsername() {
            if (memberUsername != null && !memberUsername.isBlank()) return memberUsername;
            return username != null && !username.isBlank() ? username : memberId;
        }
        public String resolvedPassword() { return vaultPassword != null && !vaultPassword.isBlank() ? vaultPassword : password; }
    }
    public record VaultOpenRequestV2(String familyCode, Long targetMemberId, String memberUsername, String vaultPassword) {}
    public record VaultOpenResponse(String vaultSessionId, String mode, String token, Long memberId, String memberCode, String ownerName, String familyCode, String familyName) {}

    public record VaultProfileResponse(Long memberId, String fullName, String username, String relationship,
                                       Instant createdAt, String familyCode, String familyName,
                                       String secretKeyEntry, String emergencyPin) {}
    public record EmergencyVaultProfileResponse(Long memberId, String name, String username,
                                                boolean emergencyMode, String emergencyPin) {}
    public record ChangeVaultPasswordRequest(@NotBlank String oldPassword,
                                             @NotBlank @Size(min = 8) String newPassword,
                                             @NotBlank String confirmPassword) {}
    public record EmergencyChangeVaultPasswordRequest(@NotBlank @Size(min = 8) String newPassword,
                                                      @NotBlank String confirmPassword) {}
    public record SimpleResponse(boolean success, String message) {}

    public record EmergencyUnlockRequest(String secretKey, String emergencySecretKey) {
        public String resolvedSecretKey() { return secretKey != null && !secretKey.isBlank() ? secretKey : emergencySecretKey; }
    }
    public record EmergencyUnlockResponse(boolean success, String mode, String emergencySessionId, Long targetMemberId,
                                          Long memberId, String memberCode, String ownerName, String familyCode,
                                          String familyName, String message) {}
}

