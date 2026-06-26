package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final CurrentUserService current;

    public AdminController(AdminService admin, CurrentUserService current) {
        this.admin = admin;
        this.current = current;
    }

    @PostMapping("/families")
    public FamilyResponse createFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return admin.createFamily(request, current.user());
    }

    @GetMapping("/families")
    public List<FamilyResponse> families() {
        return admin.listFamilies();
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return admin.dashboard();
    }

    @GetMapping("/activity")
    public List<ActivityLogResponse> activity() {
        return admin.recentActivity();
    }

    @GetMapping("/families/{familyId}/members-summary")
    public List<MemberSummary> memberSummary(@PathVariable String familyId) {
        return admin.memberSummary(familyId);
    }

    @PatchMapping("/families/{familyId}/disable")
    public FamilyResponse disableFamily(@PathVariable Long familyId) {
        return admin.disableFamily(familyId, current.user());
    }

    @PatchMapping("/families/{familyId}/enable")
    public FamilyResponse enableFamily(@PathVariable Long familyId) {
        return admin.enableFamily(familyId, current.user());
    }

    @DeleteMapping("/families/{familyId}")
    public Map<String, String> deleteFamily(@PathVariable Long familyId) {
        admin.deleteFamily(familyId, current.user());
        return Map.of("message", "Family removed safely");
    }

    @GetMapping("/families/suggest-code")
    public Map<String, String> suggestCode(@RequestParam(defaultValue = "Family") String name) {
        return Map.of("familyCode", admin.suggestFamilyCode(name));
    }
}
