package com.familyvault.dto;

import com.familyvault.entity.LegendMemoryCategory;
import com.familyvault.entity.LegendStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;

public class LegendDto {
    public record LockerRequest(@NotBlank String name, @NotBlank String relationship, LegendStatus status,
                                LocalDate dateOfBirth, LocalDate dateOfPassing, String shortDescription,
                                String profilePhotoPath) {}

    public record LockerResponse(Long id, String name, String relationship, LegendStatus status,
                                 LocalDate dateOfBirth, LocalDate dateOfPassing, String shortDescription,
                                 String profilePhotoPath, Instant createdAt, Instant updatedAt) {}

    public record MemoryRequest(LegendMemoryCategory category, @NotBlank String title, @NotBlank String content,
                                Long contributorMemberId, @NotBlank String contributorUsername,
                                String contributorRelation, LocalDate memoryDate) {}

    public record MemoryResponse(Long id, Long legendId, LegendMemoryCategory category, String title, String content,
                                 Long contributorMemberId, String contributorUsername, String contributorRelation,
                                 LocalDate memoryDate, Instant createdAt, Instant updatedAt) {}
}
