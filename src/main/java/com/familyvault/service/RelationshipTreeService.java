package com.familyvault.service;

import com.familyvault.dto.RelationshipTreeDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelationshipTreeService {
    private final FamilyTreePersonRepository people;
    private final FamilyTreeRelationRepository relations;
    private final FamilyMemberRepository members;

    public RelationshipTreeService(FamilyTreePersonRepository people, FamilyTreeRelationRepository relations,
                                   FamilyMemberRepository members) {
        this.people = people;
        this.relations = relations;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public TreeResponse tree(Family family) {
        return new TreeResponse(
                people.findByFamilyOrderByGenerationLevelAscSiblingOrderAscIdAsc(family).stream().map(this::toPerson).toList(),
                relations.findByFamilyOrderByIdAsc(family).stream().map(this::toRelation).toList());
    }

    @Transactional
    public PersonResponse createRoot(Family family, PersonRequest request) {
        if (people.countByFamily(family) > 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Family tree already has a root person");
        FamilyTreePerson person = base(family, request);
        person.setGenerationLevel(0);
        person.setSiblingOrder(1);
        person.setSpouseOrder(0);
        person.setTreeIndex("0.1");
        return toPerson(people.save(person));
    }

    @Transactional
    public PersonResponse addParent(Family family, Long selectedPersonId, PersonRequest request) {
        FamilyTreePerson selected = person(family, selectedPersonId);
        TreeRelationTag tag = parseTag(request.parentType(), request.relationTag(), "Mother".equalsIgnoreCase(request.parentType()) ? TreeRelationTag.MOTHER : TreeRelationTag.FATHER);
        FamilyTreePerson parent = base(family, request);
        parent.setRelationTag(tag);
        parent.setGender(tag == TreeRelationTag.MOTHER ? TreeGender.FEMALE : TreeGender.MALE);
        parent.setGenerationLevel(selected.getGenerationLevel() - 1);
        parent.setSiblingOrder(nextOrder(family, parent.getGenerationLevel()));
        parent.setTreeIndex(parent.getGenerationLevel() + "." + parent.getSiblingOrder());
        FamilyTreePerson saved = people.save(parent);
        relate(family, saved, selected, FamilyTreeRelationType.PARENT_OF);
        relate(family, selected, saved, FamilyTreeRelationType.CHILD_OF);
        return toPerson(saved);
    }

    @Transactional
    public PersonResponse addSpouse(Family family, Long selectedPersonId, PersonRequest request) {
        FamilyTreePerson selected = person(family, selectedPersonId);
        TreeRelationTag tag = parseTag(request.spouseType(), request.relationTag(), "Husband".equalsIgnoreCase(request.spouseType()) ? TreeRelationTag.HUSBAND : TreeRelationTag.WIFE);
        int spouseOrder = (int) relations.countByFamilyAndFromPersonAndRelationType(family, selected, FamilyTreeRelationType.SPOUSE_OF) + 1;
        FamilyTreePerson spouse = base(family, request);
        spouse.setRelationTag(tag);
        spouse.setGender(tag == TreeRelationTag.HUSBAND ? TreeGender.MALE : TreeGender.FEMALE);
        spouse.setGenerationLevel(selected.getGenerationLevel());
        spouse.setSiblingOrder(selected.getSiblingOrder());
        spouse.setSpouseOrder(spouseOrder);
        spouse.setTreeIndex(selected.getTreeIndex() + "w" + spouseOrder);
        FamilyTreePerson saved = people.save(spouse);
        relate(family, selected, saved, FamilyTreeRelationType.SPOUSE_OF);
        relate(family, saved, selected, FamilyTreeRelationType.SPOUSE_OF);
        return toPerson(saved);
    }

    @Transactional
    public PersonResponse addChild(Family family, Long selectedPersonId, PersonRequest request) {
        FamilyTreePerson selected = person(family, selectedPersonId);
        TreeRelationTag tag = parseTag(request.childType(), request.relationTag(), "Daughter".equalsIgnoreCase(request.childType()) ? TreeRelationTag.DAUGHTER : TreeRelationTag.SON);
        FamilyTreePerson child = base(family, request);
        child.setRelationTag(tag);
        child.setGender(tag == TreeRelationTag.DAUGHTER ? TreeGender.FEMALE : TreeGender.MALE);
        child.setGenerationLevel(selected.getGenerationLevel() + 1);
        child.setSiblingOrder(nextOrder(family, child.getGenerationLevel()));
        child.setSpouseOrder(0);
        child.setTreeIndex(child.getGenerationLevel() + "." + child.getSiblingOrder());
        FamilyTreePerson saved = people.save(child);
        relate(family, selected, saved, FamilyTreeRelationType.PARENT_OF);
        relate(family, saved, selected, FamilyTreeRelationType.CHILD_OF);
        return toPerson(saved);
    }

    @Transactional
    public PersonResponse update(Family family, Long personId, PersonRequest request) {
        FamilyTreePerson person = person(family, personId);
        apply(person, request);
        return toPerson(person);
    }

    @Transactional
    public void delete(Family family, Long personId) {
        FamilyTreePerson person = person(family, personId);
        relations.deleteByFamilyAndFromPersonOrFamilyAndToPerson(family, person, family, person);
        people.delete(person);
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> search(Family family, String query) {
        return people.findByFamilyAndNameContainingIgnoreCaseOrderByGenerationLevelAsc(family, query == null ? "" : query)
                .stream().map(this::toPerson).toList();
    }

    private FamilyTreePerson base(Family family, PersonRequest request) {
        FamilyTreePerson person = new FamilyTreePerson();
        person.setFamily(family);
        apply(person, request);
        return person;
    }

    private void apply(FamilyTreePerson person, PersonRequest request) {
        person.setName(request.name().trim());
        person.setLinkedFamilyMember(request.linkedFamilyMemberId() == null ? null : linkedMember(person.getFamily(), request.linkedFamilyMemberId()));
        person.setGender(request.gender() == null ? TreeGender.OTHER : request.gender());
        person.setRelationTag(request.relationTag() == null ? TreeRelationTag.OTHER : request.relationTag());
        person.setStatus(request.status() == null ? LegendStatus.ALIVE : request.status());
        person.setDateOfBirth(request.dateOfBirth());
        person.setDateOfDeath(request.dateOfDeath());
        person.setProfilePhotoPath(blankToNull(request.profilePhotoPath()));
        person.setShortNote(blankToNull(request.shortNote()));
    }

    private void relate(Family family, FamilyTreePerson from, FamilyTreePerson to, FamilyTreeRelationType type) {
        FamilyTreeRelation relation = new FamilyTreeRelation();
        relation.setFamily(family);
        relation.setFromPerson(from);
        relation.setToPerson(to);
        relation.setRelationType(type);
        relations.save(relation);
    }

    private int nextOrder(Family family, int generation) {
        return (int) people.countByFamilyAndGenerationLevel(family, generation) + 1;
    }

    private TreeRelationTag parseTag(String value, TreeRelationTag requested, TreeRelationTag fallback) {
        if (requested != null && requested != TreeRelationTag.OTHER) return requested;
        if (value == null || value.isBlank()) return fallback;
        try {
            return TreeRelationTag.valueOf(value.trim().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private FamilyTreePerson person(Family family, Long id) {
        return people.findByIdAndFamily(id, family).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tree person not found"));
    }

    private FamilyMember linkedMember(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Linked family member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Linked member belongs to another family");
        return member;
    }

    private PersonResponse toPerson(FamilyTreePerson person) {
        return new PersonResponse(person.getId(), person.getLinkedFamilyMember() == null ? null : person.getLinkedFamilyMember().getId(),
                person.getName(), person.getGender(), person.getRelationTag(), person.getStatus(),
                person.getDateOfBirth(), person.getDateOfDeath(), person.getProfilePhotoPath(), person.getShortNote(),
                person.getGenerationLevel(), person.getSiblingOrder(), person.getSpouseOrder(), person.getTreeIndex(),
                person.getCreatedAt(), person.getUpdatedAt());
    }

    private RelationResponse toRelation(FamilyTreeRelation relation) {
        return new RelationResponse(relation.getId(), relation.getFromPerson().getId(), relation.getToPerson().getId(),
                relation.getRelationType(), relation.getCreatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
