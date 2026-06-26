package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "family_tree_relations")
public class FamilyTreeRelation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_person_id", nullable = false)
    private FamilyTreePerson fromPerson;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_person_id", nullable = false)
    private FamilyTreePerson toPerson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyTreeRelationType relationType;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    public FamilyTreePerson getFromPerson() { return fromPerson; }
    public void setFromPerson(FamilyTreePerson fromPerson) { this.fromPerson = fromPerson; }
    public FamilyTreePerson getToPerson() { return toPerson; }
    public void setToPerson(FamilyTreePerson toPerson) { this.toPerson = toPerson; }
    public FamilyTreeRelationType getRelationType() { return relationType; }
    public void setRelationType(FamilyTreeRelationType relationType) { this.relationType = relationType; }
    public Instant getCreatedAt() { return createdAt; }
}
