package com.familyvault.service;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyMember;
import com.familyvault.entity.Folder;
import com.familyvault.entity.Locker;
import com.familyvault.entity.VaultFile;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.FamilyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyService {
    private final FamilyRepository families;
    private final FamilyMemberRepository members;
    private final AdminService adminService;
    private final CloudinaryStorageService storage;
    @PersistenceContext
    private EntityManager entityManager;

    public FamilyService(FamilyRepository families, FamilyMemberRepository members, AdminService adminService, CloudinaryStorageService storage) {
        this.families = families;
        this.members = members;
        this.adminService = adminService;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public TreeResponse tree(String familyCode, FamilyMember current) {
        Family family = families.findByFamilyCode(familyCode).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Family not found"));
        if (!family.getId().equals(current.getFamily().getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Cannot view another family");
        List<MemberSummary> cards = members.findByFamily(family).stream()
                .map(m -> new MemberSummary(m.getId(), m.getMemberCode(), m.getFullName(), m.getRelationship(), m.getProfilePhotoPath(), m.getId().equals(current.getId())))
                .toList();
        return new TreeResponse(family.getFamilyCode(), family.getName(), cards);
    }

    @Transactional(readOnly = true)
    public TreeResponse tree(FamilyMember current) {
        return tree(current.getFamily().getFamilyCode(), current);
    }

    @Transactional(readOnly = true)
    public TreeResponse tree(Family family) {
        List<MemberSummary> cards = members.findByFamily(family).stream()
                .map(m -> new MemberSummary(m.getId(), m.getMemberCode(), m.getFullName(), m.getRelationship(), m.getProfilePhotoPath(), false))
                .toList();
        return new TreeResponse(family.getFamilyCode(), family.getName(), cards);
    }

    @Transactional(readOnly = true)
    public List<MemberSummary> members(String familyCode, FamilyMember current) {
        return tree(familyCode, current).members();
    }

    @Transactional
    public MemberSummary addMember(AddMemberRequest request, FamilyMember current) {
        return addMember(request, current.getFamily());
    }

    @Transactional
    public MemberSummary addMember(AddMemberRequest request, Family family) {
        FamilyMember created = adminService.createMember(family,
                new MemberSeed(request.resolvedMemberId(), request.resolvedMemberId(), request.fullName(),
                        request.relationship(), request.password(), request.password()));
        return new MemberSummary(created.getId(), created.getMemberCode(), created.getFullName(),
                created.getRelationship(), created.getProfilePhotoPath(), false);
    }

    @Transactional
    public SimpleResponse deleteVault(Long memberId, Family family) {
        FamilyMember member = members.findByIdAndFamily(memberId, family)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Selected vault not found"));
        if (members.countByFamilyId(family.getId()) <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one family member vault must remain");
        }

        Locker locker = member.getLocker();
        Long lockerId = locker == null ? null : locker.getId();
        if (locker != null) {
            for (Folder folder : locker.getFolders()) {
                for (VaultFile file : folder.getFiles()) {
                    storage.deleteFile(file.getCloudinaryPublicId(), file.getCloudinaryResourceType());
                }
            }
            for (Object[] mediaFile : vaultMediaFiles(member.getId(), locker.getId())) {
                storage.deleteFile((String) mediaFile[0], (String) mediaFile[1]);
            }
        }

        cleanupMemberReferences(member.getId(), lockerId);
        members.delete(member);
        return new SimpleResponse(true, "Vault deleted successfully.");
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> vaultMediaFiles(Long memberId, Long lockerId) {
        if (lockerId == null) return List.of();
        return entityManager.createNativeQuery("select cloudinary_public_id, cloudinary_resource_type from vault_media_items where owner_member_id = :memberId or locker_id = :lockerId")
                .setParameter("memberId", memberId)
                .setParameter("lockerId", lockerId)
                .getResultList();
    }

    private void cleanupMemberReferences(Long memberId, Long lockerId) {
        if (lockerId != null) {
            executeNative("update digital_wills set related_file_id = null where related_file_id in (select vf.id from vault_files vf join folders f on vf.folder_id = f.id where f.locker_id = :lockerId)", "lockerId", lockerId);
            executeNative("update digital_wills set related_folder_id = null where related_folder_id in (select id from folders where locker_id = :lockerId)", "lockerId", lockerId);
            executeNative("update my_first_memories set attachment_file_id = null where attachment_file_id in (select vf.id from vault_files vf join folders f on vf.folder_id = f.id where f.locker_id = :lockerId)", "lockerId", lockerId);
            executeNative("delete from vault_media_items where owner_member_id = :memberId or locker_id = :lockerId", "memberId", memberId, "lockerId", lockerId);
        }
        executeNative("update digital_wills set nominee_member_id = null where nominee_member_id = :memberId", "memberId", memberId);
        executeNative("delete from digital_wills where owner_member_id = :memberId", "memberId", memberId);
        executeNative("update legacy_messages set recipient_member_id = null where recipient_member_id = :memberId", "memberId", memberId);
        executeNative("delete from legacy_messages where owner_member_id = :memberId", "memberId", memberId);
        executeNative("delete from my_first_memories where owner_member_id = :memberId", "memberId", memberId);
        executeNative("delete from family_public_profiles where member_id = :memberId", "memberId", memberId);
        executeNative("delete from family_relations where member_a_id = :memberId or member_b_id = :memberId", "memberId", memberId);
        executeNative("update family_tree_people set linked_family_member_id = null where linked_family_member_id = :memberId", "memberId", memberId);
        executeNative("update legend_memories set contributor_member_id = null where contributor_member_id = :memberId", "memberId", memberId);
        executeNative("delete from emergency_approvals where approver_id = :memberId", "memberId", memberId);
        executeNative("delete from emergency_approvals where request_id in (select id from emergency_access_requests where target_member_id = :memberId or requested_by_id = :memberId)", "memberId", memberId);
        executeNative("delete from emergency_access_requests where target_member_id = :memberId or requested_by_id = :memberId", "memberId", memberId);
        executeNative("delete from emergency_access_logs where target_member_id = :memberId or requested_by_member_id = :memberId", "memberId", memberId);
        executeNative("delete from recovery_shares where target_member_id = :memberId or holder_member_id = :memberId", "memberId", memberId);
    }

    private void executeNative(String sql, Object... params) {
        var query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.length; i += 2) {
            query.setParameter((String) params[i], params[i + 1]);
        }
        query.executeUpdate();
    }
}
