package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emergency_approvals", uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "approver_id"}))
public class EmergencyApproval {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private EmergencyAccessRequest request;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private FamilyMember approver;
    @Column(nullable = false)
    private Instant approvedAt = Instant.now();

    public Long getId() { return id; }
    public EmergencyAccessRequest getRequest() { return request; }
    public void setRequest(EmergencyAccessRequest request) { this.request = request; }
    public FamilyMember getApprover() { return approver; }
    public void setApprover(FamilyMember approver) { this.approver = approver; }
    public Instant getApprovedAt() { return approvedAt; }
}
