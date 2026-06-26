package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_history_entries")
public class FamilyHistoryEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyHistorySectionType sectionType;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String mainText;
    private String fieldOne;
    private String fieldTwo;
    private String fieldThree;
    private String fieldFour;
    private String fieldFive;
    @Column(nullable = false)
    private String contributorName;
    private String contributorRelation;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public FamilyHistorySectionType getSectionType() { return sectionType; }
    public void setSectionType(FamilyHistorySectionType sectionType) { this.sectionType = sectionType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMainText() { return mainText; }
    public void setMainText(String mainText) { this.mainText = mainText; }
    public String getFieldOne() { return fieldOne; }
    public void setFieldOne(String fieldOne) { this.fieldOne = fieldOne; }
    public String getFieldTwo() { return fieldTwo; }
    public void setFieldTwo(String fieldTwo) { this.fieldTwo = fieldTwo; }
    public String getFieldThree() { return fieldThree; }
    public void setFieldThree(String fieldThree) { this.fieldThree = fieldThree; }
    public String getFieldFour() { return fieldFour; }
    public void setFieldFour(String fieldFour) { this.fieldFour = fieldFour; }
    public String getFieldFive() { return fieldFive; }
    public void setFieldFive(String fieldFive) { this.fieldFive = fieldFive; }
    public String getContributorName() { return contributorName; }
    public void setContributorName(String contributorName) { this.contributorName = contributorName; }
    public String getContributorRelation() { return contributorRelation; }
    public void setContributorRelation(String contributorRelation) { this.contributorRelation = contributorRelation; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
