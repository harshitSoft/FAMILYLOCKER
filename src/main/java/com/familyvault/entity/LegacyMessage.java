package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "legacy_messages")
public class LegacyMessage {
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
    @JoinColumn(name = "recipient_member_id")
    private FamilyMember recipientMember;
    private String recipientName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegacyReleaseType releaseType = LegacyReleaseType.AFTER_EMERGENCY;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;
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
    public FamilyMember getRecipientMember() { return recipientMember; }
    public void setRecipientMember(FamilyMember recipientMember) { this.recipientMember = recipientMember; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public LegacyReleaseType getReleaseType() { return releaseType; }
    public void setReleaseType(LegacyReleaseType releaseType) { this.releaseType = releaseType; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
