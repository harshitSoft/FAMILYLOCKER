package com.familyvault.dto;

import com.familyvault.entity.FamilyRelationType;
import java.time.Instant;

public class FamilyHistoryDto {
    public record HistoryRequest(String historyText, String originPlace, String migrationStory,
                                 String familyDiseaseHistory, String kundaliNotes) {}

    public record HistoryResponse(Long id, String historyText, String originPlace, String migrationStory,
                                  String familyDiseaseHistory, String kundaliNotes, Instant createdAt,
                                  Instant updatedAt) {}

    public record RelationRequest(Long memberAId, Long memberBId, FamilyRelationType relationType, String notes) {}

    public record RelationResponse(Long id, Long memberAId, String memberAName, Long memberBId, String memberBName,
                                   FamilyRelationType relationType, String notes) {}
}
