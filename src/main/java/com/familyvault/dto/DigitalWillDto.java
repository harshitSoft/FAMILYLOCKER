package com.familyvault.dto;

import com.familyvault.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class DigitalWillDto {
    public record Request(@NotBlank String title, @NotBlank String message, Long nomineeMemberId, String nomineeName,
                          Long relatedFolderId, Long relatedFileId, Priority priority, Boolean visibilityAfterEmergency) {}

    public record Response(Long id, Long ownerMemberId, String title, String message, Long nomineeMemberId,
                           String nomineeName, Long relatedFolderId, String relatedFolderName,
                           Long relatedFileId, String relatedFileName, Priority priority,
                           boolean visibilityAfterEmergency, Instant createdAt, Instant updatedAt) {}
}
