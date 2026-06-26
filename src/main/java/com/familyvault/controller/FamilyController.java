package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.FamilyService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
    private final FamilyService familyService;
    private final CurrentUserService current;

    public FamilyController(FamilyService familyService, CurrentUserService current) {
        this.familyService = familyService;
        this.current = current;
    }

    @GetMapping("/tree")
    public TreeResponse tree() {
        return familyService.tree(current.family());
    }

    @GetMapping("/members")
    public List<MemberSummary> members() {
        return familyService.tree(current.family()).members();
    }

    @PostMapping("/members/add")
    public MemberSummary addMember(@ModelAttribute AddMemberRequest request) {
        return familyService.addMember(request, current.family());
    }

    @PostMapping(value = "/members/add", consumes = "application/json")
    public MemberSummary addMemberJson(@RequestBody AddMemberRequest request) {
        return familyService.addMember(request, current.family());
    }

    @DeleteMapping("/vaults/{memberId}")
    public SimpleResponse deleteVault(@PathVariable Long memberId) {
        return familyService.deleteVault(memberId, current.family());
    }

    @GetMapping("/{familyCode}/tree")
    public TreeResponse treeByCode(@PathVariable String familyCode) {
        return familyService.tree(familyCode, current.member());
    }

    @GetMapping("/{familyCode}/members")
    public List<MemberSummary> membersByCode(@PathVariable String familyCode) {
        return familyService.members(familyCode, current.member());
    }
}
