package com.familyvault.dto;

import com.familyvault.entity.MyFirstCategory;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;

public class MyFirstDto {
    public record Request(@NotBlank String title, @NotBlank String description, LocalDate eventDate,
                          MyFirstCategory category, Long attachmentFileId, Boolean visibleAfterEmergency) {}

    public record Response(Long id, Long ownerMemberId, String title, String description, LocalDate eventDate,
                           MyFirstCategory category, Long attachmentFileId, String attachmentFileName,
                           boolean visibleAfterEmergency, Instant createdAt, Instant updatedAt) {}
}
