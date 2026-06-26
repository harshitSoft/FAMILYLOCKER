package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final CurrentUserService current;

    public AuthController(AuthService auth, CurrentUserService current) {
        this.auth = auth;
        this.current = current;
    }

    @PostMapping("/super-admin/login")
    public AuthResponse superAdminLogin(@Valid @RequestBody LoginRequest request) {
        return auth.superAdminLogin(request);
    }

    @PostMapping("/member/login")
    public AuthResponse memberLogin(@Valid @RequestBody MemberLoginRequest request) {
        return auth.memberLogin(request);
    }

    @PostMapping("/family-login")
    public AuthResponse familyLogin(@Valid @RequestBody FamilyLoginRequest request) {
        return auth.familyLogin(request);
    }

    @PostMapping("/family/login")
    public AuthResponse familyLoginAlias(@Valid @RequestBody FamilyLoginRequest request) {
        return auth.familyLogin(request);
    }

    @PostMapping("/member/register")
    public AuthResponse register(@Valid @RequestBody MemberRegisterRequest request) {
        return auth.register(request);
    }

    @GetMapping("/me")
    public MeResponse me() {
        return auth.me(current.user());
    }
}
