package com.familyvault.dto;

import com.familyvault.entity.LegacyReleaseType;
import com.familyvault.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class LegacyMessageDto {
    public record Request(@NotBlank String title, @NotBlank String message, Long recipientMemberId,
                          String recipientName, LegacyReleaseType releaseType, Priority priority) {}

    public record Response(Long id, Long ownerMemberId, String title, String message, Long recipientMemberId,
                           String recipientName, LegacyReleaseType releaseType, Priority priority,
                           Instant createdAt, Instant updatedAt) {}
}
