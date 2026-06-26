package com.familyvault.dto;

import com.familyvault.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public class FamilyKnowledgeDto {
    public record HistoryEntryRequest(String title, String mainText, String fieldOne, String fieldTwo,
                                      String fieldThree, String fieldFour, String fieldFive,
                                      @NotBlank String contributorName, String contributorRelation) {}

    public record HistoryEntryResponse(Long id, FamilyHistorySectionType sectionType, String title, String mainText,
                                       String fieldOne, String fieldTwo, String fieldThree, String fieldFour,
                                       String fieldFive, String contributorName, String contributorRelation,
                                       Instant createdAt, Instant updatedAt) {}

    public record ImportantDateRequest(@NotBlank String title, String personName, @NotNull LocalDate date,
                                       ImportantDateCategory category, String description,
                                       @NotBlank String contributorName, String contributorRelation) {}

    public record ImportantDateResponse(Long id, String title, String personName, LocalDate date,
                                        ImportantDateCategory category, String description,
                                        String contributorName, String contributorRelation,
                                        Instant createdAt, Instant updatedAt, int daysUntil) {}
}
