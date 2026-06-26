package com.familyvault.config;

import com.familyvault.dto.ApiDtos.MemberSeed;
import com.familyvault.entity.*;
import com.familyvault.repository.AppUserRepository;
import com.familyvault.repository.FamilyMemberRepository;
import com.familyvault.repository.FamilyRepository;
import com.familyvault.repository.FolderRepository;
import com.familyvault.service.AdminService;
import com.familyvault.service.EmergencyPinService;
import com.familyvault.util.CryptoUtil;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(AppUserRepository users, FamilyRepository families, FamilyMemberRepository members,
                           FolderRepository folders, PasswordEncoder encoder, AdminService adminService, CryptoUtil crypto,
                           EmergencyPinService emergencyPins,
                           TransactionTemplate transactionTemplate,
                           @Value("${app.super-admin.username}") String superAdminUsername,
                           @Value("${app.super-admin.password:}") String superAdminPassword,
                           @Value("${app.super-admin.password-hash:}") String superAdminPasswordHash,
                           @Value("${app.super-admin.reset-password-on-startup:false}") boolean resetPasswordOnStartup) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            String username = requiredSuperAdminUsername(superAdminUsername);
            String resolvedHash = resolveSuperAdminHash(superAdminPassword, superAdminPasswordHash, encoder);
            AppUser superAdmin = users.findByUsername(username).orElseGet(AppUser::new);
            boolean newUser = superAdmin.getId() == null;
            superAdmin.setUsername(username);
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setEnabled(true);
            if (newUser || resetPasswordOnStartup || superAdmin.getPasswordHash() == null || superAdmin.getPasswordHash().isBlank()) {
                superAdmin.setPasswordHash(resolvedHash);
            }
            if (superAdmin.getPasswordHash() == null || superAdmin.getPasswordHash().isBlank()) {
                throw new IllegalStateException("Resolved super admin password hash is blank");
            }
            users.save(superAdmin);
            if (families.count() == 0) {
                Family family = new Family();
                family.setName("Kapoor Family");
                family.setFamilyCode("KAPOOR-001");
                family.setFamilyPasswordHash(encoder.encode("Family@12345"));
                families.save(family);
                AppUser familyUser = new AppUser();
                familyUser.setUsername("FAMILY:KAPOOR-001");
                familyUser.setPasswordHash(family.getFamilyPasswordHash());
                familyUser.setRole(Role.FAMILY_MEMBER);
                users.save(familyUser);
                List.of(
                        new MemberSeed("MEM-001", "MEM-001", "Aarav Kapoor", "Father", "Member@12345", "Member@12345"),
                        new MemberSeed("MEM-002", "MEM-002", "Meera Kapoor", "Mother", "Member@12345", "Member@12345"),
                        new MemberSeed("MEM-003", "MEM-003", "Isha Kapoor", "Daughter", "Member@12345", "Member@12345")
                ).forEach(seed -> adminService.createMember(family, seed));
            }
            for (Family family : families.findAll()) {
                if ("KAPOOR-001".equals(family.getFamilyCode())) {
                    family.setFamilyPasswordHash(encoder.encode("Family@12345"));
                    families.save(family);
                }
                users.findByUsername("FAMILY:" + family.getFamilyCode()).orElseGet(() -> {
                    AppUser familyUser = new AppUser();
                    familyUser.setUsername("FAMILY:" + family.getFamilyCode());
                    familyUser.setPasswordHash(family.getFamilyPasswordHash() == null ? encoder.encode("Family@12345") : family.getFamilyPasswordHash());
                    familyUser.setRole(Role.FAMILY_MEMBER);
                    return users.save(familyUser);
                });
                users.findByUsername("FAMILY:" + family.getFamilyCode()).ifPresent(user -> {
                    user.setPasswordHash(family.getFamilyPasswordHash());
                    users.save(user);
                });
                if (family.getFamilyPasswordHash() == null || family.getFamilyPasswordHash().isBlank()) {
                    family.setFamilyPasswordHash(encoder.encode("Family@12345"));
                    families.save(family);
                }
                for (FamilyMember member : members.findByFamily(family)) {
                    if (member.getEncryptedEmergencySecretPart() == null || member.getEncryptedEmergencySecretPart().isBlank()) {
                        member.setEncryptedEmergencySecretPart(crypto.encryptText("Member@12345"));
                        members.save(member);
                    }
                    ensureDefaultFolder(member, folders, "PRIVATE FOLDER", true);
                    ensureDefaultFolder(member, folders, "OFFICIAL DOCUMENTS", false);
                }
                emergencyPins.ensureEmergencyPinsForFamily(family);
            }
        });
    }

    private String resolveSuperAdminHash(String plainPassword, String passwordHash, PasswordEncoder encoder) {
        if (passwordHash != null && !passwordHash.isBlank()) return passwordHash.trim();
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalStateException("Set app.super-admin.password or app.super-admin.password-hash in application.properties");
        }
        return encoder.encode(plainPassword);
    }

    private String requiredSuperAdminUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Set app.super-admin.username in application.properties");
        }
        return username.trim();
    }

    private void ensureDefaultFolder(FamilyMember member, FolderRepository folders, String name, boolean hidden) {
        boolean exists = folders.findByLockerOrderByCreatedAtAsc(member.getLocker()).stream()
                .anyMatch(folder -> folder.getName().equalsIgnoreCase(name));
        if (exists) return;
        Folder folder = new Folder();
        folder.setName(name);
        folder.setDefaultFolder(true);
        folder.setHidden(hidden);
        folder.setLocker(member.getLocker());
        folders.save(folder);
    }
}
