package com.familyvault.controller;

import com.familyvault.dto.PublicProfileDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.PublicProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicProfileController {
    private final PublicProfileService profiles;
    private final CurrentUserService current;

    public PublicProfileController(PublicProfileService profiles, CurrentUserService current) {
        this.profiles = profiles;
        this.current = current;
    }

    @GetMapping({"/api/family/members/{memberId}/public-profile", "/api/family/member/{memberId}/public-profile"})
    public Response get(@PathVariable Long memberId) {
        return profiles.get(current.family(), memberId);
    }

    @GetMapping("/api/vault/public-profile")
    public Response mine() {
        return profiles.mine(current.member());
    }

    @PutMapping("/api/vault/public-profile")
    public Response update(@RequestBody Request request) {
        return profiles.save(current.member(), request);
    }
}
