package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "families", indexes = @Index(name = "idx_family_code", columnList = "familyCode", unique = true))
public class Family {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String familyCode;
    @Column(nullable = false)
    private String name;
    @Column
    private String familyPasswordHash;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false)
    private boolean blocked = false;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FamilyMember> members = new ArrayList<>();

    public Long getId() { return id; }
    public String getFamilyCode() { return familyCode; }
    public void setFamilyCode(String familyCode) { this.familyCode = familyCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFamilyPasswordHash() { return familyPasswordHash; }
    public void setFamilyPasswordHash(String familyPasswordHash) { this.familyPasswordHash = familyPasswordHash; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public Instant getCreatedAt() { return createdAt; }
    public List<FamilyMember> getMembers() { return members; }
}
