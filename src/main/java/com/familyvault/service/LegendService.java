package com.familyvault.service;

import com.familyvault.dto.LegendDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegendService {
    private final LegendLockerRepository legends;
    private final LegendMemoryRepository memories;
    private final FamilyMemberRepository members;

    public LegendService(LegendLockerRepository legends, LegendMemoryRepository memories, FamilyMemberRepository members) {
        this.legends = legends;
        this.memories = memories;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public List<LockerResponse> list(Family family) {
        return legends.findByFamilyOrderByCreatedAtDesc(family).stream().map(this::toLocker).toList();
    }

    @Transactional(readOnly = true)
    public LockerResponse get(Family family, Long id) {
        return toLocker(legend(family, id));
    }

    @Transactional
    public LockerResponse create(Family family, LockerRequest request) {
        LegendLocker locker = new LegendLocker();
        locker.setFamily(family);
        apply(locker, request);
        return toLocker(legends.save(locker));
    }

    @Transactional
    public LockerResponse update(Family family, Long id, LockerRequest request) {
        LegendLocker locker = legend(family, id);
        apply(locker, request);
        return toLocker(locker);
    }

    @Transactional
    public void delete(Family family, Long id) {
        legends.delete(legend(family, id));
    }

    @Transactional(readOnly = true)
    public List<MemoryResponse> memories(Family family, Long legendId) {
        return memories.findByLegendLockerOrderByCreatedAtDesc(legend(family, legendId)).stream().map(this::toMemory).toList();
    }

    @Transactional
    public MemoryResponse createMemory(Family family, Long legendId, MemoryRequest request) {
        LegendLocker locker = legend(family, legendId);
        LegendMemory memory = new LegendMemory();
        memory.setLegendLocker(locker);
        applyMemory(family, memory, request);
        return toMemory(memories.save(memory));
    }

    @Transactional
    public MemoryResponse updateMemory(Family family, Long legendId, Long memoryId, MemoryRequest request) {
        LegendMemory memory = memory(family, legendId, memoryId);
        applyMemory(family, memory, request);
        return toMemory(memory);
    }

    @Transactional
    public void deleteMemory(Family family, Long legendId, Long memoryId) {
        memories.delete(memory(family, legendId, memoryId));
    }

    private void apply(LegendLocker locker, LockerRequest request) {
        locker.setName(request.name().trim());
        locker.setRelationship(request.relationship().trim());
        locker.setStatus(request.status() == null ? LegendStatus.PASSED_AWAY : request.status());
        locker.setDateOfBirth(request.dateOfBirth());
        locker.setDateOfPassing(request.dateOfPassing());
        locker.setShortDescription(request.shortDescription());
        locker.setProfilePhotoPath(request.profilePhotoPath());
    }

    private void applyMemory(Family family, LegendMemory memory, MemoryRequest request) {
        memory.setCategory(request.category() == null ? LegendMemoryCategory.CUSTOM : request.category());
        memory.setTitle(request.title().trim());
        memory.setContent(request.content().trim());
        memory.setContributorUsername(request.contributorUsername().trim());
        memory.setContributorRelation(request.contributorRelation());
        memory.setMemoryDate(request.memoryDate());
        memory.setContributorMember(request.contributorMemberId() == null ? null : member(family, request.contributorMemberId()));
    }

    private LegendLocker legend(Family family, Long id) {
        return legends.findByIdAndFamily(id, family).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Legend not found"));
    }

    private LegendMemory memory(Family family, Long legendId, Long memoryId) {
        return memories.findByIdAndLegendLocker(memoryId, legend(family, legendId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Legend memory not found"));
    }

    private FamilyMember member(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private LockerResponse toLocker(LegendLocker locker) {
        return new LockerResponse(locker.getId(), locker.getName(), locker.getRelationship(), locker.getStatus(),
                locker.getDateOfBirth(), locker.getDateOfPassing(), locker.getShortDescription(),
                locker.getProfilePhotoPath(), locker.getCreatedAt(), locker.getUpdatedAt());
    }

    private MemoryResponse toMemory(LegendMemory memory) {
        return new MemoryResponse(memory.getId(), memory.getLegendLocker().getId(), memory.getCategory(),
                memory.getTitle(), memory.getContent(),
                memory.getContributorMember() == null ? null : memory.getContributorMember().getId(),
                memory.getContributorUsername(), memory.getContributorRelation(), memory.getMemoryDate(),
                memory.getCreatedAt(), memory.getUpdatedAt());
    }
}
