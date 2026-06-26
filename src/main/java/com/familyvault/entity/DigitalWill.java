package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "digital_wills")
public class DigitalWill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private FamilyMember ownerMember;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nominee_member_id")
    private FamilyMember nomineeMember;
    private String nomineeName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_folder_id")
    private Folder relatedFolder;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_file_id")
    private VaultFile relatedFile;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;
    @Column(nullable = false)
    private boolean visibilityAfterEmergency = true;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public FamilyMember getOwnerMember() { return ownerMember; }
    public void setOwnerMember(FamilyMember ownerMember) { this.ownerMember = ownerMember; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public FamilyMember getNomineeMember() { return nomineeMember; }
    public void setNomineeMember(FamilyMember nomineeMember) { this.nomineeMember = nomineeMember; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public Folder getRelatedFolder() { return relatedFolder; }
    public void setRelatedFolder(Folder relatedFolder) { this.relatedFolder = relatedFolder; }
    public VaultFile getRelatedFile() { return relatedFile; }
    public void setRelatedFile(VaultFile relatedFile) { this.relatedFile = relatedFile; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public boolean isVisibilityAfterEmergency() { return visibilityAfterEmergency; }
    public void setVisibilityAfterEmergency(boolean visibilityAfterEmergency) { this.visibilityAfterEmergency = visibilityAfterEmergency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
