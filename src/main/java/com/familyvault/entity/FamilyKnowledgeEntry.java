package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_knowledge_entries")
public class FamilyKnowledgeEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @Column(name = "question", nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    @Column(nullable = false)
    private String keyword;
    @Column(name = "wisdom_type", length = 120)
    private String type;
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String explanation;
    @Column(nullable = false, length = 120)
    private String category = "Custom";
    @Column(name = "suggested_by", nullable = false)
    private String contributorName;
    @Column(name = "contributor_member_id")
    private Long contributorMemberId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyKnowledgeSourceType sourceType = FamilyKnowledgeSourceType.MANUAL;
    private Long sourceId;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public Long getFamilyId() { return family == null ? null : family.getId(); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getKeywords() { return keyword; }
    public void setKeywords(String keywords) { this.keyword = keywords; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContributorName() { return contributorName; }
    public void setContributorName(String contributorName) { this.contributorName = contributorName; }
    public Long getContributorMemberId() { return contributorMemberId; }
    public void setContributorMemberId(Long contributorMemberId) { this.contributorMemberId = contributorMemberId; }
    public FamilyKnowledgeSourceType getSourceType() { return sourceType; }
    public void setSourceType(FamilyKnowledgeSourceType sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
