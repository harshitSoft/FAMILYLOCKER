package com.familyvault.dto;

import com.familyvault.entity.FamilyKnowledgeSourceType;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class FamilyAiGuideDto {
    public record KnowledgeRequest(Long contributorMemberId, @NotBlank String contributorName,
                                   @NotBlank String category, @NotBlank String type,
                                   @NotBlank String title, @NotBlank String explanation,
                                   String description, String keywords) {}

    public record KnowledgeResponse(Long id, Long familyId, Long contributorMemberId, String contributorName,
                                    String category, String type, String title, String explanation,
                                    String description, String keywords, FamilyKnowledgeSourceType sourceType,
                                    Long sourceId, Instant createdAt, Instant updatedAt) {}

    public record KnowledgeSaveResponse(boolean success, String message, int pointsEarned, KnowledgeResponse entry) {}

    public record RankingResponse(Long id, Long contributorMemberId, String contributorName,
                                  int totalPoints, int totalContributions, Instant updatedAt) {}

    public record GuideTitleResponse(Long id, String title) {}

    public record GuideAnswerResponse(Long id, String category, String type, String title, String explanation,
                                      String contributorName, Instant createdAt) {}

    public record SearchResult(String title, String description, String explanation, String category,
                               FamilyKnowledgeSourceType sourceType, String sourceLabel, String contributorName,
                               Instant createdAt, int score, List<String> matchedKeywords) {}

    public record SearchResponse(String query, String answerSummary, List<SearchResult> results) {}
}
