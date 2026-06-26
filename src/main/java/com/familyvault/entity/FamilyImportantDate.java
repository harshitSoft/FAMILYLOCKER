package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "family_important_dates")
public class FamilyImportantDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @Column(nullable = false)
    private String title;
    private String personName;
    @Column(nullable = false)
    private LocalDate dateValue;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportantDateCategory category = ImportantDateCategory.CUSTOM;
    private String description;
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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public LocalDate getDateValue() { return dateValue; }
    public void setDateValue(LocalDate dateValue) { this.dateValue = dateValue; }
    public ImportantDateCategory getCategory() { return category; }
    public void setCategory(ImportantDateCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContributorName() { return contributorName; }
    public void setContributorName(String contributorName) { this.contributorName = contributorName; }
    public String getContributorRelation() { return contributorRelation; }
    public void setContributorRelation(String contributorRelation) { this.contributorRelation = contributorRelation; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
