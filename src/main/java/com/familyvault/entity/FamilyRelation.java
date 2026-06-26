package com.familyvault.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "family_relations")
public class FamilyRelation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_a_id", nullable = false)
    private FamilyMember memberA;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_b_id", nullable = false)
    private FamilyMember memberB;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRelationType relationType = FamilyRelationType.OTHER;
    private String notes;

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public FamilyMember getMemberA() { return memberA; }
    public void setMemberA(FamilyMember memberA) { this.memberA = memberA; }
    public FamilyMember getMemberB() { return memberB; }
    public void setMemberB(FamilyMember memberB) { this.memberB = memberB; }
    public FamilyRelationType getRelationType() { return relationType; }
    public void setRelationType(FamilyRelationType relationType) { this.relationType = relationType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
