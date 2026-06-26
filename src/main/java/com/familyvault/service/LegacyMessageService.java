package com.familyvault.service;

import com.familyvault.dto.LegacyMessageDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.LegacyMessageRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegacyMessageService {
    private final LegacyMessageRepository messages;
    private final FamilyMemberRepository members;
    private final EmergencyVaultSessionService emergencySessions;

    public LegacyMessageService(LegacyMessageRepository messages, FamilyMemberRepository members,
                                EmergencyVaultSessionService emergencySessions) {
        this.messages = messages;
        this.members = members;
        this.emergencySessions = emergencySessions;
    }

    @Transactional(readOnly = true)
    public List<Response> list(FamilyMember owner) {
        return messages.findByOwnerMemberOrderByPriorityDescUpdatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Response create(FamilyMember owner, Request request) {
        LegacyMessage message = new LegacyMessage();
        message.setOwnerMember(owner);
        apply(owner, message, request);
        return toResponse(messages.save(message));
    }

    @Transactional
    public Response update(FamilyMember owner, Long id, Request request) {
        LegacyMessage message = owned(owner, id);
        apply(owner, message, request);
        return toResponse(message);
    }

    @Transactional
    public void delete(FamilyMember owner, Long id) {
        messages.delete(owned(owner, id));
    }

    @Transactional(readOnly = true)
    public List<Response> emergencyList(Family currentFamily, Long targetMemberId, String sessionId) {
        FamilyMember target = target(currentFamily, targetMemberId);
        emergencySessions.validate(sessionId, targetMemberId, currentFamily);
        return messages.findByOwnerMemberAndReleaseTypeOrderByPriorityDescUpdatedAtDesc(target, LegacyReleaseType.AFTER_EMERGENCY).stream()
                .map(this::toResponse)
                .toList();
    }

    private void apply(FamilyMember owner, LegacyMessage message, Request request) {
        String title = required(request.title(), "Title");
        String body = required(request.message(), "Message");
        message.setTitle(title);
        message.setMessage(body);
        message.setRecipientMember(request.recipientMemberId() == null ? null : target(owner.getFamily(), request.recipientMemberId()));
        message.setRecipientName(blankToNull(request.recipientName()));
        message.setReleaseType(request.releaseType() == null ? LegacyReleaseType.AFTER_EMERGENCY : request.releaseType());
        message.setPriority(request.priority() == null ? Priority.MEDIUM : request.priority());
    }

    private LegacyMessage owned(FamilyMember owner, Long id) {
        return messages.findByIdAndOwnerMember(id, owner).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Legacy message not found"));
    }

    private FamilyMember target(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private Response toResponse(LegacyMessage message) {
        return new Response(
                message.getId(),
                message.getOwnerMember().getId(),
                message.getTitle(),
                message.getMessage(),
                message.getRecipientMember() == null ? null : message.getRecipientMember().getId(),
                message.getRecipientName(),
                message.getReleaseType(),
                message.getPriority(),
                message.getCreatedAt(),
                message.getUpdatedAt());
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
