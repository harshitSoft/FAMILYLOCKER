package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_members", uniqueConstraints = @UniqueConstraint(columnNames = {"family_id", "memberCode"}))
public class FamilyMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String memberCode;
    @Column(nullable = false)
    private String fullName;
    private String relationship;
    private String profilePhotoPath;
    @Column(columnDefinition = "TEXT")
    private String encryptedEmergencySecretPart;
    @Column(name = "emergency_pin", length = 4)
    private String emergencyPin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Locker locker;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }
    public String getEncryptedEmergencySecretPart() { return encryptedEmergencySecretPart; }
    public void setEncryptedEmergencySecretPart(String encryptedEmergencySecretPart) { this.encryptedEmergencySecretPart = encryptedEmergencySecretPart; }
    public String getEmergencyPin() { return emergencyPin; }
    public void setEmergencyPin(String emergencyPin) { this.emergencyPin = emergencyPin; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Locker getLocker() { return locker; }
    public void setLocker(Locker locker) { this.locker = locker; }
    public Instant getCreatedAt() { return createdAt; }
}
