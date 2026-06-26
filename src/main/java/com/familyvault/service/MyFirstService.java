package com.familyvault.service;

import com.familyvault.dto.MyFirstDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyFirstService {
    private final MyFirstMemoryRepository firsts;
    private final FamilyMemberRepository members;
    private final VaultFileRepository files;
    private final EmergencyVaultSessionService emergencySessions;

    public MyFirstService(MyFirstMemoryRepository firsts, FamilyMemberRepository members, VaultFileRepository files,
                          EmergencyVaultSessionService emergencySessions) {
        this.firsts = firsts;
        this.members = members;
        this.files = files;
        this.emergencySessions = emergencySessions;
    }

    @Transactional(readOnly = true)
    public List<Response> list(FamilyMember owner) {
        return firsts.findByOwnerMemberOrderByEventDateDescCreatedAtDesc(owner).stream()
                .map(memory -> toResponse(memory, true))
                .toList();
    }

    @Transactional
    public Response create(FamilyMember owner, Request request) {
        MyFirstMemory memory = new MyFirstMemory();
        memory.setOwnerMember(owner);
        apply(owner, memory, request);
        return toResponse(firsts.save(memory), true);
    }

    @Transactional
    public Response update(FamilyMember owner, Long id, Request request) {
        MyFirstMemory memory = owned(owner, id);
        apply(owner, memory, request);
        return toResponse(memory, true);
    }

    @Transactional
    public void delete(FamilyMember owner, Long id) {
        firsts.delete(owned(owner, id));
    }

    @Transactional(readOnly = true)
    public List<Response> emergencyList(Family currentFamily, Long targetMemberId, String sessionId) {
        FamilyMember target = target(currentFamily, targetMemberId);
        emergencySessions.validate(sessionId, targetMemberId, currentFamily);
        return firsts.findByOwnerMemberAndVisibleAfterEmergencyTrueOrderByEventDateDescCreatedAtDesc(target).stream()
                .map(memory -> toResponse(memory, false))
                .toList();
    }

    private void apply(FamilyMember owner, MyFirstMemory memory, Request request) {
        memory.setTitle(request.title().trim());
        memory.setDescription(request.description().trim());
        memory.setEventDate(request.eventDate());
        memory.setCategory(request.category() == null ? MyFirstCategory.CUSTOM : request.category());
        memory.setVisibleAfterEmergency(request.visibleAfterEmergency() == null || request.visibleAfterEmergency());
        memory.setAttachmentFile(request.attachmentFileId() == null ? null : file(owner, request.attachmentFileId()));
    }

    private MyFirstMemory owned(FamilyMember owner, Long id) {
        return firsts.findByIdAndOwnerMember(id, owner).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "First memory not found"));
    }

    private VaultFile file(FamilyMember owner, Long id) {
        return files.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attachment file not found"));
    }

    private FamilyMember target(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private Response toResponse(MyFirstMemory memory, boolean includePrivateAttachment) {
        VaultFile file = memory.getAttachmentFile();
        boolean exposeFile = file != null && (includePrivateAttachment || (!file.isHidden() && !file.getFolder().isHidden()));
        return new Response(memory.getId(), memory.getOwnerMember().getId(), memory.getTitle(), memory.getDescription(),
                memory.getEventDate(), memory.getCategory(), exposeFile ? file.getId() : null,
                exposeFile ? file.getOriginalName() : null, memory.isVisibleAfterEmergency(),
                memory.getCreatedAt(), memory.getUpdatedAt());
    }
}
