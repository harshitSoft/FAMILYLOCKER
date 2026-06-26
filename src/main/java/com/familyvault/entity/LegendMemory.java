package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "legend_memories")
public class LegendMemory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legend_locker_id", nullable = false)
    private LegendLocker legendLocker;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegendMemoryCategory category = LegendMemoryCategory.CUSTOM;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_member_id")
    private FamilyMember contributorMember;
    @Column(nullable = false)
    private String contributorUsername;
    private String contributorRelation;
    private LocalDate memoryDate;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public LegendLocker getLegendLocker() { return legendLocker; }
    public void setLegendLocker(LegendLocker legendLocker) { this.legendLocker = legendLocker; }
    public LegendMemoryCategory getCategory() { return category; }
    public void setCategory(LegendMemoryCategory category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public FamilyMember getContributorMember() { return contributorMember; }
    public void setContributorMember(FamilyMember contributorMember) { this.contributorMember = contributorMember; }
    public String getContributorUsername() { return contributorUsername; }
    public void setContributorUsername(String contributorUsername) { this.contributorUsername = contributorUsername; }
    public String getContributorRelation() { return contributorRelation; }
    public void setContributorRelation(String contributorRelation) { this.contributorRelation = contributorRelation; }
    public LocalDate getMemoryDate() { return memoryDate; }
    public void setMemoryDate(LocalDate memoryDate) { this.memoryDate = memoryDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
