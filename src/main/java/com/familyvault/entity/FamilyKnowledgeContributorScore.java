package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_knowledge_contributor_scores",
        uniqueConstraints = @UniqueConstraint(name = "uk_family_knowledge_score_contributor", columnNames = {"family_id", "contributor_member_id"}))
public class FamilyKnowledgeContributorScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @Column(nullable = false)
    private String contributorName;
    @Column(name = "contributor_member_id")
    private Long contributorMemberId;
    @Column(nullable = false)
    private int totalPoints;
    @Column(nullable = false)
    private int totalContributions;
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public Long getFamilyId() { return family == null ? null : family.getId(); }
    public String getContributorName() { return contributorName; }
    public void setContributorName(String contributorName) { this.contributorName = contributorName; }
    public Long getContributorMemberId() { return contributorMemberId; }
    public void setContributorMemberId(Long contributorMemberId) { this.contributorMemberId = contributorMemberId; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getTotalContributions() { return totalContributions; }
    public void setTotalContributions(int totalContributions) { this.totalContributions = totalContributions; }
    public Instant getUpdatedAt() { return updatedAt; }
}
