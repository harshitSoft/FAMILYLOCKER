package com.familyvault.dto;

import com.familyvault.entity.FamilyMediaType;
import java.time.Instant;

public class MediaDtos {
    public record FamilyGalleryResponse(Long id, String title, String description, FamilyMediaType mediaType,
                                        String originalFileName, String contentType, long fileSize,
                                        String uploadedByName, Long uploadedByMemberId, Instant createdAt) {}

    public record FamilyGalleryUploadResponse(boolean success, String message, FamilyGalleryResponse item) {}

    public record VaultMediaResponse(Long id, Long ownerMemberId, String title, String description,
                                     FamilyMediaType mediaType, String originalFileName, String contentType,
                                     long fileSize, boolean privateHidden, Instant createdAt) {}
}
