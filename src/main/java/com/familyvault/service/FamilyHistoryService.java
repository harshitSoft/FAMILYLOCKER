package com.familyvault.service;

import com.familyvault.dto.FamilyHistoryDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyHistoryService {
    private final FamilyHistoryRepository histories;
    private final FamilyRelationRepository relations;
    private final FamilyMemberRepository members;

    public FamilyHistoryService(FamilyHistoryRepository histories, FamilyRelationRepository relations, FamilyMemberRepository members) {
        this.histories = histories;
        this.relations = relations;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public HistoryResponse get(Family family) {
        return toHistory(history(family));
    }

    @Transactional
    public HistoryResponse save(Family family, HistoryRequest request) {
        FamilyHistory history = history(family);
        history.setHistoryText(request.historyText());
        history.setOriginPlace(request.originPlace());
        history.setMigrationStory(request.migrationStory());
        history.setFamilyDiseaseHistory(request.familyDiseaseHistory());
        history.setKundaliNotes(request.kundaliNotes());
        return toHistory(history);
    }

    @Transactional(readOnly = true)
    public List<RelationResponse> relations(Family family) {
        return relations.findByFamilyOrderByIdAsc(family).stream().map(this::toRelation).toList();
    }

    @Transactional
    public RelationResponse createRelation(Family family, RelationRequest request) {
        FamilyRelation relation = new FamilyRelation();
        relation.setFamily(family);
        apply(family, relation, request);
        return toRelation(relations.save(relation));
    }

    @Transactional
    public RelationResponse updateRelation(Family family, Long id, RelationRequest request) {
        FamilyRelation relation = relation(family, id);
        apply(family, relation, request);
        return toRelation(relation);
    }

    @Transactional
    public void deleteRelation(Family family, Long id) {
        relations.delete(relation(family, id));
    }

    private void apply(Family family, FamilyRelation relation, RelationRequest request) {
        relation.setMemberA(member(family, request.memberAId()));
        relation.setMemberB(member(family, request.memberBId()));
        relation.setRelationType(request.relationType() == null ? FamilyRelationType.OTHER : request.relationType());
        relation.setNotes(request.notes());
    }

    private FamilyHistory history(Family family) {
        return histories.findByFamily(family).orElseGet(() -> {
            FamilyHistory history = new FamilyHistory();
            history.setFamily(family);
            return histories.save(history);
        });
    }

    private FamilyRelation relation(Family family, Long id) {
        return relations.findByIdAndFamily(id, family).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Relation not found"));
    }

    private FamilyMember member(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private HistoryResponse toHistory(FamilyHistory history) {
        return new HistoryResponse(history.getId(), history.getHistoryText(), history.getOriginPlace(),
                history.getMigrationStory(), history.getFamilyDiseaseHistory(), history.getKundaliNotes(),
                history.getCreatedAt(), history.getUpdatedAt());
    }

    private RelationResponse toRelation(FamilyRelation relation) {
        return new RelationResponse(relation.getId(), relation.getMemberA().getId(), relation.getMemberA().getFullName(),
                relation.getMemberB().getId(), relation.getMemberB().getFullName(), relation.getRelationType(), relation.getNotes());
    }
}
