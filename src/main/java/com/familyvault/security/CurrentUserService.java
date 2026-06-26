package com.familyvault.security;

import com.familyvault.entity.AppUser;
import com.familyvault.entity.FamilyMember;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.AppUserRepository;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.FamilyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final AppUserRepository users;
    private final FamilyMemberRepository members;
    private final FamilyRepository families;

    public CurrentUserService(AppUserRepository users, FamilyMemberRepository members, FamilyRepository families) {
        this.users = users;
        this.members = members;
        this.families = families;
    }

    public AppUser user() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return users.findByUsername(auth.getName()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public FamilyMember member() {
        return members.findByUser(user()).orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Family member account required"));
    }

    public com.familyvault.entity.Family family() {
        AppUser user = user();
        if (user.getUsername().startsWith("FAMILY:")) {
            String code = user.getUsername().substring("FAMILY:".length());
            return families.findByFamilyCode(code).orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Family account not found"));
        }
        return member().getFamily();
    }
}
