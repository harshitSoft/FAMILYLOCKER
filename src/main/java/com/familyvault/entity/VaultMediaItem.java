package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vault_media_items")
public class VaultMediaItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private FamilyMember ownerMember;
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
    private boolean privateHidden;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Locker getLocker() { return locker; }
    public void setLocker(Locker locker) { this.locker = locker; }
    public FamilyMember getOwnerMember() { return ownerMember; }
    public void setOwnerMember(FamilyMember ownerMember) { this.ownerMember = ownerMember; }
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
    public boolean isPrivateHidden() { return privateHidden; }
    public void setPrivateHidden(boolean privateHidden) { this.privateHidden = privateHidden; }
    public Instant getCreatedAt() { return createdAt; }
}
