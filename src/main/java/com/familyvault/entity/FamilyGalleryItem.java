package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_gallery_items")
public class FamilyGalleryItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @Column(nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyMediaType mediaType;
    @Column(nullable = false)
    private String originalFileName;
    @Column(nullable = false)
    private String storedFileName;
    @Column(nullable = false)
    private String storedPath;
    private String cloudinaryPublicId;
    @Column(length = 1000)
    private String cloudinarySecureUrl;
    private String cloudinaryResourceType;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private long fileSize;
    @Column(nullable = false)
    private String uploadedByName;
    private Long uploadedByMemberId;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FamilyMediaType getMediaType() { return mediaType; }
    public void setMediaType(FamilyMediaType mediaType) { this.mediaType = mediaType; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    public String getCloudinarySecureUrl() { return cloudinarySecureUrl; }
    public void setCloudinarySecureUrl(String cloudinarySecureUrl) { this.cloudinarySecureUrl = cloudinarySecureUrl; }
    public String getCloudinaryResourceType() { return cloudinaryResourceType; }
    public void setCloudinaryResourceType(String cloudinaryResourceType) { this.cloudinaryResourceType = cloudinaryResourceType; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }
    public Long getUploadedByMemberId() { return uploadedByMemberId; }
    public void setUploadedByMemberId(Long uploadedByMemberId) { this.uploadedByMemberId = uploadedByMemberId; }
    public Instant getCreatedAt() { return createdAt; }
}
