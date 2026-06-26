package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lockers")
public class Locker {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private FamilyMember owner;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedVaultKey;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>();

    public Long getId() { return id; }
    public FamilyMember getOwner() { return owner; }
    public void setOwner(FamilyMember owner) { this.owner = owner; }
    public String getEncryptedVaultKey() { return encryptedVaultKey; }
    public void setEncryptedVaultKey(String encryptedVaultKey) { this.encryptedVaultKey = encryptedVaultKey; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Folder> getFolders() { return folders; }
}
