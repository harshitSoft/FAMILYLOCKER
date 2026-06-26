package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "family_public_profiles", uniqueConstraints = @UniqueConstraint(columnNames = "member_id"))
public class FamilyVisibleProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private FamilyMember member;
    private LocalDate dateOfBirth;
    private String name;
    private String bloodGroup;
    private String education;
    private String contact;
    private String work;
    private String phone;
    private String email;
    private String favoriteFood;
    @Column(columnDefinition = "TEXT")
    private String recipes;
    @Column(columnDefinition = "TEXT")
    private String lifeLessons;
    @Column(columnDefinition = "TEXT")
    private String lifeRegrets;
    @Column(columnDefinition = "TEXT")
    private String importantAdvice;
    private String hobbies;
    @Column(name = "basic_info", columnDefinition = "TEXT")
    private String basicInfo;
    @Column(columnDefinition = "TEXT")
    private String publicBio;
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public FamilyMember getMember() { return member; }
    public void setMember(FamilyMember member) {
        this.member = member;
    }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getWork() { return work; }
    public void setWork(String work) { this.work = work; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFavoriteFood() { return favoriteFood; }
    public void setFavoriteFood(String favoriteFood) { this.favoriteFood = favoriteFood; }
    public String getRecipes() { return recipes; }
    public void setRecipes(String recipes) { this.recipes = recipes; }
    public String getLifeLessons() { return lifeLessons; }
    public void setLifeLessons(String lifeLessons) { this.lifeLessons = lifeLessons; }
    public String getLifeRegrets() { return lifeRegrets; }
    public void setLifeRegrets(String lifeRegrets) { this.lifeRegrets = lifeRegrets; }
    public String getImportantAdvice() { return importantAdvice; }
    public void setImportantAdvice(String importantAdvice) { this.importantAdvice = importantAdvice; }
    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }
    public String getBasicInfo() { return basicInfo; }
    public void setBasicInfo(String basicInfo) { this.basicInfo = basicInfo; }
    public String getPublicBio() { return publicBio; }
    public void setPublicBio(String publicBio) { this.publicBio = publicBio; }
    public Instant getUpdatedAt() { return updatedAt; }
}
