package com.familyvault.dto;

import com.familyvault.entity.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class RelationshipTreeDto {
    public record PersonRequest(Long linkedFamilyMemberId, @NotBlank String name, TreeGender gender,
                                TreeRelationTag relationTag, LegendStatus status, LocalDate dateOfBirth,
                                LocalDate dateOfDeath, String profilePhotoPath, String shortNote,
                                String parentType, String spouseType, String childType) {}

    public record PersonResponse(Long id, Long linkedFamilyMemberId, String name, TreeGender gender,
                                 TreeRelationTag relationTag, LegendStatus status, LocalDate dateOfBirth,
                                 LocalDate dateOfDeath, String profilePhotoPath, String shortNote,
                                 int generationLevel, int siblingOrder, int spouseOrder, String treeIndex,
                                 Instant createdAt, Instant updatedAt) {}

    public record RelationResponse(Long id, Long fromPersonId, Long toPersonId,
                                   FamilyTreeRelationType relationType, Instant createdAt) {}

    public record TreeResponse(List<PersonResponse> people, List<RelationResponse> relations) {}
}
