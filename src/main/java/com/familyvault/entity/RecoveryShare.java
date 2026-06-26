package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recovery_shares", uniqueConstraints = @UniqueConstraint(columnNames = {"target_member_id", "holder_member_id"}))
public class RecoveryShare {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private FamilyMember targetMember;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holder_member_id", nullable = false)
    private FamilyMember holderMember;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedShare;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public FamilyMember getTargetMember() { return targetMember; }
    public void setTargetMember(FamilyMember targetMember) { this.targetMember = targetMember; }
    public FamilyMember getHolderMember() { return holderMember; }
    public void setHolderMember(FamilyMember holderMember) { this.holderMember = holderMember; }
    public String getEncryptedShare() { return encryptedShare; }
    public void setEncryptedShare(String encryptedShare) { this.encryptedShare = encryptedShare; }
    public Instant getCreatedAt() { return createdAt; }
}
