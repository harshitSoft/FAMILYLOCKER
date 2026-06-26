package com.familyvault.service;

import com.familyvault.dto.MediaDtos.VaultMediaResponse;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.VaultMediaRepository;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VaultMediaService {
    private final VaultMediaRepository media;
    private final FamilyMemberRepository members;
    private final CloudinaryStorageService storage;

    public VaultMediaService(VaultMediaRepository media, FamilyMemberRepository members, CloudinaryStorageService storage) {
        this.media = media;
        this.members = members;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<VaultMediaResponse> list(Long memberId, FamilyMember current, boolean emergency) {
        FamilyMember owner = target(memberId, current.getFamily());
        if (!emergency && !owner.getId().equals(current.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Normal vault media access requires owner authentication");
        return (emergency ? media.findByLockerAndPrivateHiddenFalseOrderByCreatedAtDesc(owner.getLocker()) : media.findByLockerOrderByCreatedAtDesc(owner.getLocker()))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VaultMediaResponse> emergencyList(Long memberId, Family currentFamily) {
        FamilyMember owner = target(memberId, currentFamily);
        return media.findByLockerAndPrivateHiddenFalseOrderByCreatedAtDesc(owner.getLocker()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public VaultMediaResponse upload(FamilyMember owner, String title, String description, boolean privateHidden, MultipartFile file) {
        FamilyGalleryService.validateTitle(title);
        FamilyGalleryService.validateFile(file);
        try {
            String safeName = FamilyGalleryService.safeName(file.getOriginalFilename());
            FamilyMediaType mediaType = FamilyGalleryService.mediaType(file.getContentType(), safeName);
            CloudinaryStorageService.StoredCloudFile stored = storage.uploadFile(file, "vault-media/member-" + owner.getId());
            VaultMediaItem item = new VaultMediaItem();
            item.setLocker(owner.getLocker());
            item.setOwnerMember(owner);
            item.setTitle(title.trim());
            item.setDescription(FamilyGalleryService.blankToNull(description));
            item.setMediaType(mediaType);
            item.setOriginalFileName(safeName);
            item.setStoredFileName(stored.publicId());
            item.setStoredPath(stored.secureUrl());
            item.setCloudinaryPublicId(stored.publicId());
            item.setCloudinarySecureUrl(stored.secureUrl());
            item.setCloudinaryResourceType(stored.resourceType());
            item.setContentType(FamilyGalleryService.contentType(file.getContentType(), mediaType));
            item.setFileSize(file.getSize());
            item.setPrivateHidden(privateHidden);
            return toResponse(media.save(item));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault media upload failed");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> preview(Long memberId, Long id, FamilyMember current, boolean emergency) {
        VaultMediaItem item = item(memberId, id, current.getFamily(), emergency);
        if (!emergency && !item.getOwnerMember().getId().equals(current.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Normal vault media access requires owner authentication");
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), true);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> download(Long memberId, Long id, FamilyMember current, boolean emergency) {
        VaultMediaItem item = item(memberId, id, current.getFamily(), emergency);
        if (!emergency && !item.getOwnerMember().getId().equals(current.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Normal vault media access requires owner authentication");
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), false);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> emergencyPreview(Long memberId, Long id, Family currentFamily) {
        VaultMediaItem item = item(memberId, id, currentFamily, true);
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), true);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> emergencyDownload(Long memberId, Long id, Family currentFamily) {
        VaultMediaItem item = item(memberId, id, currentFamily, true);
        return MediaResponseUtil.file(storage.downloadBytes(item.getCloudinarySecureUrl()), item.getContentType(), item.getOriginalFileName(), false);
    }

    @Transactional
    public void delete(Long id, FamilyMember current) {
        VaultMediaItem item = media.findByIdAndLocker(id, current.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vault media not found"));
        storage.deleteFile(item.getCloudinaryPublicId(), item.getCloudinaryResourceType());
        media.delete(item);
    }

    private VaultMediaItem item(Long memberId, Long id, Family family, boolean emergency) {
        FamilyMember owner = target(memberId, family);
        VaultMediaItem item = media.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vault media not found"));
        if (emergency && item.isPrivateHidden()) throw new ApiException(HttpStatus.FORBIDDEN, "Private media is excluded");
        return item;
    }

    private FamilyMember target(Long memberId, Family family) {
        FamilyMember owner = members.findById(memberId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found"));
        if (!owner.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return owner;
    }

    private VaultMediaResponse toResponse(VaultMediaItem item) {
        return new VaultMediaResponse(item.getId(), item.getOwnerMember().getId(), item.getTitle(), item.getDescription(),
                item.getMediaType(), item.getOriginalFileName(), item.getContentType(), item.getFileSize(),
                item.isPrivateHidden(), item.getCreatedAt());
    }
}
