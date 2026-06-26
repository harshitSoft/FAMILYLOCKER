package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emergency_access_logs")
public class EmergencyAccessLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_member_id")
    private FamilyMember requestedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private FamilyMember targetMember;
    @Column(nullable = false)
    private boolean success;
    private String ipAddress;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public FamilyMember getRequestedBy() { return requestedBy; }
    public void setRequestedBy(FamilyMember requestedBy) { this.requestedBy = requestedBy; }
    public FamilyMember getTargetMember() { return targetMember; }
    public void setTargetMember(FamilyMember targetMember) { this.targetMember = targetMember; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Instant getCreatedAt() { return createdAt; }
}
