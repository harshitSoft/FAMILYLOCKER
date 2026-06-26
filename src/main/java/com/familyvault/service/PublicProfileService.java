package com.familyvault.service;

import com.familyvault.dto.PublicProfileDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.FamilyVisibleProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicProfileService {
    private final FamilyVisibleProfileRepository profiles;
    private final FamilyMemberRepository members;

    public PublicProfileService(FamilyVisibleProfileRepository profiles, FamilyMemberRepository members) {
        this.profiles = profiles;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public Response get(Family family, Long memberId) {
        FamilyMember member = member(family, memberId);
        return profiles.findByMember(member)
                .map(this::toResponse)
                .orElseGet(() -> emptyResponse(member));
    }

    @Transactional(readOnly = true)
    public Response mine(FamilyMember owner) {
        return profiles.findByMember(owner)
                .map(this::toResponse)
                .orElseGet(() -> emptyResponse(owner));
    }

    @Transactional
    public Response save(FamilyMember owner, Request request) {
        FamilyVisibleProfile profile = profiles.findByMember(owner).orElseGet(() -> {
            FamilyVisibleProfile next = new FamilyVisibleProfile();
            next.setMember(owner);
            return next;
        });
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setName(blankToNull(request.name()));
        profile.setBloodGroup(blankToNull(request.bloodGroup()));
        profile.setEducation(blankToNull(request.education()));
        profile.setContact(blankToNull(request.contact()));
        profile.setWork(blankToNull(request.work()));
        profile.setBasicInfo(blankToNull(first(request.basicInfo(), request.basicInformation())));
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setFavoriteFood(request.favoriteFood());
        profile.setRecipes(request.recipes());
        profile.setLifeLessons(request.lifeLessons());
        profile.setLifeRegrets(request.lifeRegrets());
        profile.setImportantAdvice(request.importantAdvice());
        profile.setHobbies(request.hobbies());
        profile.setPublicBio(request.publicBio());
        return toResponse(profiles.save(profile));
    }

    private FamilyMember member(Family family, Long id) {
        FamilyMember member = members.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        if (!member.getFamily().getId().equals(family.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Different family");
        return member;
    }

    private Response toResponse(FamilyVisibleProfile profile) {
        FamilyMember member = profile.getMember();
        String basicInfo = first(profile.getBasicInfo(), profile.getPublicBio());
        return new Response(true, member.getId(), member.getFullName(), member.getRelationship(), member.getMemberCode(),
                profile.getDateOfBirth(), profile.getName(), profile.getBloodGroup(),
                profile.getEducation(), first(profile.getContact(), profile.getPhone(), profile.getEmail()),
                profile.getWork(), basicInfo, basicInfo,
                profile.getPhone(), profile.getEmail(), profile.getFavoriteFood(),
                profile.getRecipes(), profile.getLifeLessons(), profile.getLifeRegrets(), profile.getImportantAdvice(),
                profile.getHobbies(), profile.getPublicBio(), profile.getUpdatedAt(), null);
    }

    private Response emptyResponse(FamilyMember member) {
        return new Response(false, member.getId(), member.getFullName(), member.getRelationship(), member.getMemberCode(),
                null, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", null,
                "No public information has been shared.");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String first(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
