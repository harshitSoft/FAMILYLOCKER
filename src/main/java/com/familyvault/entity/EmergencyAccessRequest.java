package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emergency_access_requests")
public class EmergencyAccessRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private FamilyMember targetMember;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private FamilyMember requestedBy;
    @Column(nullable = false)
    private int threshold;
    @Column(nullable = false)
    private boolean unlocked;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    private Instant unlockedAt;
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyApproval> approvals = new ArrayList<>();

    public Long getId() { return id; }
    public FamilyMember getTargetMember() { return targetMember; }
    public void setTargetMember(FamilyMember targetMember) { this.targetMember = targetMember; }
    public FamilyMember getRequestedBy() { return requestedBy; }
    public void setRequestedBy(FamilyMember requestedBy) { this.requestedBy = requestedBy; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Instant unlockedAt) { this.unlockedAt = unlockedAt; }
    public List<EmergencyApproval> getApprovals() { return approvals; }
}
