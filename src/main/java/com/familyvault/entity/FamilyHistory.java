package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_histories")
public class FamilyHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false, unique = true)
    private Family family;
    @Column(columnDefinition = "TEXT")
    private String historyText;
    private String originPlace;
    @Column(columnDefinition = "TEXT")
    private String migrationStory;
    @Column(columnDefinition = "TEXT")
    private String familyDiseaseHistory;
    @Column(columnDefinition = "TEXT")
    private String kundaliNotes;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public String getHistoryText() { return historyText; }
    public void setHistoryText(String historyText) { this.historyText = historyText; }
    public String getOriginPlace() { return originPlace; }
    public void setOriginPlace(String originPlace) { this.originPlace = originPlace; }
    public String getMigrationStory() { return migrationStory; }
    public void setMigrationStory(String migrationStory) { this.migrationStory = migrationStory; }
    public String getFamilyDiseaseHistory() { return familyDiseaseHistory; }
    public void setFamilyDiseaseHistory(String familyDiseaseHistory) { this.familyDiseaseHistory = familyDiseaseHistory; }
    public String getKundaliNotes() { return kundaliNotes; }
    public void setKundaliNotes(String kundaliNotes) { this.kundaliNotes = kundaliNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
