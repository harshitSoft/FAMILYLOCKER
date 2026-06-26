package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "my_first_memories")
public class MyFirstMemory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private FamilyMember ownerMember;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    private LocalDate eventDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MyFirstCategory category = MyFirstCategory.CUSTOM;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_file_id")
    private VaultFile attachmentFile;
    @Column(nullable = false)
    private boolean visibleAfterEmergency = true;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public FamilyMember getOwnerMember() { return ownerMember; }
    public void setOwnerMember(FamilyMember ownerMember) { this.ownerMember = ownerMember; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public MyFirstCategory getCategory() { return category; }
    public void setCategory(MyFirstCategory category) { this.category = category; }
    public VaultFile getAttachmentFile() { return attachmentFile; }
    public void setAttachmentFile(VaultFile attachmentFile) { this.attachmentFile = attachmentFile; }
    public boolean isVisibleAfterEmergency() { return visibleAfterEmergency; }
    public void setVisibleAfterEmergency(boolean visibleAfterEmergency) { this.visibleAfterEmergency = visibleAfterEmergency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
