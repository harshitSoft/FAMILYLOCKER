package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "family_tree_people")
public class FamilyTreePerson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_family_member_id")
    private FamilyMember linkedFamilyMember;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreeGender gender = TreeGender.OTHER;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreeRelationTag relationTag = TreeRelationTag.OTHER;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegendStatus status = LegendStatus.ALIVE;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private String profilePhotoPath;
    @Column(length = 2000)
    private String shortNote;
    @Column(nullable = false)
    private int generationLevel;
    @Column(nullable = false)
    private int siblingOrder = 1;
    @Column(nullable = false)
    private int spouseOrder = 0;
    @Column(nullable = false)
    private String treeIndex;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public FamilyMember getLinkedFamilyMember() { return linkedFamilyMember; }
    public void setLinkedFamilyMember(FamilyMember linkedFamilyMember) { this.linkedFamilyMember = linkedFamilyMember; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TreeGender getGender() { return gender; }
    public void setGender(TreeGender gender) { this.gender = gender; }
    public TreeRelationTag getRelationTag() { return relationTag; }
    public void setRelationTag(TreeRelationTag relationTag) { this.relationTag = relationTag; }
    public LegendStatus getStatus() { return status; }
    public void setStatus(LegendStatus status) { this.status = status; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }
    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }
    public String getShortNote() { return shortNote; }
    public void setShortNote(String shortNote) { this.shortNote = shortNote; }
    public int getGenerationLevel() { return generationLevel; }
    public void setGenerationLevel(int generationLevel) { this.generationLevel = generationLevel; }
    public int getSiblingOrder() { return siblingOrder; }
    public void setSiblingOrder(int siblingOrder) { this.siblingOrder = siblingOrder; }
    public int getSpouseOrder() { return spouseOrder; }
    public void setSpouseOrder(int spouseOrder) { this.spouseOrder = spouseOrder; }
    public String getTreeIndex() { return treeIndex; }
    public void setTreeIndex(String treeIndex) { this.treeIndex = treeIndex; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
