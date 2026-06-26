package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vault_files")
public class VaultFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String originalName;
    @Column(nullable = false)
    private String storedName;
    private String cloudinaryPublicId;
    @Column(length = 1000)
    private String cloudinarySecureUrl;
    private String cloudinaryResourceType;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    @Column(nullable = false)
    private boolean hidden;
    @Column(nullable = false)
    private String checksumSha256;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    public String getCloudinarySecureUrl() { return cloudinarySecureUrl; }
    public void setCloudinarySecureUrl(String cloudinarySecureUrl) { this.cloudinarySecureUrl = cloudinarySecureUrl; }
    public String getCloudinaryResourceType() { return cloudinaryResourceType; }
    public void setCloudinaryResourceType(String cloudinaryResourceType) { this.cloudinaryResourceType = cloudinaryResourceType; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public String getChecksumSha256() { return checksumSha256; }
    public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }
    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }
    public Instant getCreatedAt() { return createdAt; }
}
