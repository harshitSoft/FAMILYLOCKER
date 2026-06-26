package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.AdminService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {
    private final AdminService admin;
    private final CurrentUserService current;

    public SuperAdminController(AdminService admin, CurrentUserService current) {
        this.admin = admin;
        this.current = current;
    }

    @DeleteMapping("/families/{familyId}")
    public void deleteFamily(@PathVariable Long familyId) {
        admin.deleteFamily(familyId, current.user());
    }

    @PatchMapping("/families/{familyId}/block")
    public FamilyResponse blockFamily(@PathVariable Long familyId) {
        return admin.blockFamily(familyId, current.user());
    }

    @PatchMapping("/families/{familyId}/unblock")
    public FamilyResponse unblockFamily(@PathVariable Long familyId) {
        return admin.unblockFamily(familyId, current.user());
    }

    @GetMapping("/activity")
    public List<ActivityLogResponse> activity() {
        return admin.recentActivity();
    }
}
