package com.familyvault.service;

import com.familyvault.dto.MediaDtos.FamilyGalleryResponse;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyGalleryRepository;
import com.familyvault.repository.FamilyMemberRepository;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FamilyGalleryService {
    private static final long MAX_MEDIA_BYTES = 100L * 1024L * 1024L;
    private final FamilyGalleryRepository gallery;
    private final FamilyMemberRepository members;
    private final CloudinaryStorageService storage;

    public FamilyGalleryService(FamilyGalleryRepository gallery, FamilyMemberRepository members, CloudinaryStorageService storage) {
        this.gallery = gallery;
        this.members = members;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<FamilyGalleryResponse> list(Family family) {
        return gallery.findByFamilyOrderByCreatedAtDesc(family).stream().map(this::toResponse).toList();
    }

    @Transactional
    public FamilyGalleryResponse upload(Family family, String title, String description, String uploadedByName, MultipartFile file) {
        validateTitle(title);
        String uploaderName = required(uploadedByName, "Uploaded by");
        validateFile(file);
        try {
            String safeName = safeName(file.getOriginalFilename());
            FamilyMediaType mediaType = mediaType(file.getContentType(), safeName);
            CloudinaryStorageService.StoredCloudFile stored = storage.uploadFile(file, "family-gallery/family-" + family.getId());
            FamilyGalleryItem item = new FamilyGalleryItem();
            item.setFamily(family);
            item.setTitle(title.trim());
            item.setDescription(blankToNull(description));
            item.setMediaType(mediaType);
            item.setOriginalFileName(safeName);
            item.setStoredFileName(stored.publicId());
            item.setStoredPath(stored.secureUrl());
            item.setCloudinaryPublicId(stored.publicId());
            item.setCloudinarySecureUrl(stored.secureUrl());
            item.setCloudinaryResourceType(stored.resourceType());
            item.setContentType(contentType(file.getContentType(), mediaType));
            item.setFileSize(file.getSize());
            item.setUploadedByMemberId(null);
            item.setUploadedByName(uploaderName);
            return toResponse(gallery.save(item));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Gallery upload failed");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> preview(Family family, Long id) {
        FamilyGalleryItem item = item(family, id);
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), true);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> download(Family family, Long id) {
        FamilyGalleryItem item = item(family, id);
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), false);
    }

    @Transactional
    public void delete(Family family, Long id) {
        FamilyGalleryItem item = item(family, id);
        storage.deleteFile(item.getCloudinaryPublicId(), item.getCloudinaryResourceType());
        gallery.delete(item);
    }

    private FamilyGalleryItem item(Family family, Long id) {
        return gallery.findByIdAndFamily(id, family).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gallery item not found"));
    }

    private FamilyGalleryResponse toResponse(FamilyGalleryItem item) {
        return new FamilyGalleryResponse(item.getId(), item.getTitle(), item.getDescription(), item.getMediaType(),
                item.getOriginalFileName(), item.getContentType(), item.getFileSize(),
                item.getUploadedByName(), item.getUploadedByMemberId(), item.getCreatedAt());
    }

    static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "File is required");
        if (file.getSize() > MAX_MEDIA_BYTES) throw new ApiException(HttpStatus.BAD_REQUEST, "File too large");
    }

    static FamilyMediaType mediaType(String contentType, String fileName) {
        String type = contentType == null ? "" : contentType.toLowerCase();
        String name = fileName == null ? "" : fileName.toLowerCase();
        if (type.startsWith("image/") && (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/webp"))) return FamilyMediaType.IMAGE;
        if (type.startsWith("video/") && (type.equals("video/mp4") || type.equals("video/webm"))) return FamilyMediaType.VIDEO;
        if (type.startsWith("audio/") && (type.equals("audio/mpeg") || type.equals("audio/mp3") || type.equals("audio/wav") || type.equals("audio/ogg"))) return FamilyMediaType.AUDIO;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) return FamilyMediaType.IMAGE;
        if (name.endsWith(".mp4") || name.endsWith(".webm")) return FamilyMediaType.VIDEO;
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg")) return FamilyMediaType.AUDIO;
        throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported media type");
    }

    static String contentType(String contentType, FamilyMediaType mediaType) {
        if (contentType != null && !contentType.isBlank()) return contentType;
        return switch (mediaType) {
            case IMAGE -> "image/jpeg";
            case VIDEO -> "video/mp4";
            case AUDIO -> "audio/mpeg";
        };
    }

    static String safeName(String name) {
        String value = name == null || name.isBlank() ? "media" : name;
        return value.replaceAll("[\\\\/\\r\\n]", "_");
    }

    static String extension(String name) {
        int index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(index) : ".bin";
    }

    static void validateTitle(String title) {
        if (title == null || title.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "Title is required");
    }

    static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, label + " is required");
        return value.trim();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
