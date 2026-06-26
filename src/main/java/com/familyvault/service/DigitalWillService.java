package com.familyvault.service;

import com.familyvault.dto.DigitalWillDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigitalWillService {
    private final DigitalWillRepository wills;
    private final FamilyMemberRepository members;
    private final FolderRepository folders;
    private final VaultFileRepository files;
    private final EmergencyVaultSessionService emergencySessions;

    public DigitalWillService(DigitalWillRepository wills, FamilyMemberRepository members, FolderRepository folders,
                              VaultFileRepository files, EmergencyVaultSessionService emergencySessions) {
        this.wills = wills;
        this.members = members;
        this.folders = folders;
        this.files = files;
        this.emergencySessions = emergencySessions;
    }

    @Transactional(readOnly = true)
    public List<Response> list(FamilyMember owner) {
        return wills.findByOwnerMemberOrderByPriorityDescUpdatedAtDesc(owner).stream()
                .map(will -> toResponse(will, true))
                .toList();
    }

    @Transactional
    public Response create(FamilyMember owner, Request request) {
        DigitalWill will = new DigitalWill();
        will.setOwnerMember(owner);
        apply(owner, will, request);
        return toResponse(wills.save(will), true);
    }

    @Transactional
    public Response update(FamilyMember owner, Long id, Request request) {
        DigitalWill will = owned(owner, id);
        apply(owner, will, request);
        return toResponse(will, true);
    }

    @Transactional
    public void delete(FamilyMember owner, Long id) {
        wills.delete(owned(owner, id));
    }

    @Transactional(readOnly = true)
    public List<Response> emergencyList(Family currentFamily, Long targetMemberId, String sessionId) {
        FamilyMember target = target(currentFamily, targetMemberId);
        emergencySessions.validate(sessionId, targetMemberId, currentFamily);
        return wills.findByOwnerMemberAndVisibilityAfterEmergencyTrueOrderByPriorityDescUpdatedAtDesc(target).stream()
                .map(will -> toResponse(will, false))
                .toList();
    }

    private void apply(FamilyMember owner, DigitalWill will, Request request) {
        String title = required(request.title(), "Title");
        String message = required(request.message(), "Message");
        will.setTitle(title);
        will.setMessage(message);
        will.setPriority(request.priority() == null ? Priority.MEDIUM : request.priority());
        will.setVisibilityAfterEmergency(request.visibilityAfterEmergency() == null || request.visibilityAfterEmergency());
        will.setNomineeMember(request.nomineeMemberId() == null ? null : target(owner.getFamily(), request.nomineeMemberId()));
        will.setNomineeName(blankToNull(request.nomineeName()));
        will.setRelatedFolder(request.relatedFolderId() == null ? null : folder(owner, request.relatedFolderId()));
        will.setRelatedFile(request.relatedFileId() == null ? null : file(owner, request.relatedFileId()));
    }

    private DigitalWill owned(FamilyMember owner, Long id) {
        return wills.findByIdAndOwnerMember(id, owner).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Digital will not found"));
    }

    private FamilyMember target(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private Folder folder(FamilyMember owner, Long id) {
        return folders.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Related folder not found"));
    }

    private VaultFile file(FamilyMember owner, Long id) {
        return files.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Related file not found"));
    }

    private Response toResponse(DigitalWill will, boolean includePrivateReferences) {
        Folder relatedFolder = will.getRelatedFolder();
        VaultFile relatedFile = will.getRelatedFile();
        boolean exposeFolder = relatedFolder != null && (includePrivateReferences || !relatedFolder.isHidden());
        boolean exposeFile = relatedFile != null && (includePrivateReferences || (!relatedFile.isHidden() && !relatedFile.getFolder().isHidden()));
        return new Response(
                will.getId(),
                will.getOwnerMember().getId(),
                will.getTitle(),
                will.getMessage(),
                will.getNomineeMember() == null ? null : will.getNomineeMember().getId(),
                will.getNomineeName(),
                exposeFolder ? relatedFolder.getId() : null,
                exposeFolder ? relatedFolder.getName() : null,
                exposeFile ? relatedFile.getId() : null,
                exposeFile ? relatedFile.getOriginalName() : null,
                will.getPriority(),
                will.isVisibilityAfterEmergency(),
                will.getCreatedAt(),
                will.getUpdatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, label + " is required");
        }
        return value.trim();
    }
}
