package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vault")
public class VaultController {
    private final VaultService vault;
    private final CurrentUserService current;

    public VaultController(VaultService vault, CurrentUserService current) {
        this.vault = vault;
        this.current = current;
    }

    @PostMapping("/open")
    public VaultOpenResponse open(@Valid @RequestBody VaultOpenRequest request) {
        return vault.open(request, current.family());
    }

    @PostMapping("/{memberId}/emergency-unlock")
    public EmergencyUnlockResponse emergencyUnlock(@PathVariable Long memberId, @RequestBody EmergencyUnlockRequest request, HttpServletRequest servletRequest) {
        return vault.emergencyUnlock(memberId, request, servletRequest);
    }

    @GetMapping("/profile")
    public VaultProfileResponse profile() {
        return vault.profile(current.member());
    }

    @GetMapping("/emergency/profile")
    public EmergencyVaultProfileResponse emergencyProfile(
            @RequestHeader("X-Emergency-Session") String emergencySessionId) {
        return vault.emergencyProfile(emergencySessionId, current.family());
    }

    @PatchMapping("/profile/change-password")
    public SimpleResponse changePassword(@Valid @RequestBody ChangeVaultPasswordRequest request) {
        return vault.changePassword(request, current.member());
    }

    @PatchMapping("/emergency/change-password")
    public SimpleResponse emergencyChangePassword(
            @RequestHeader("X-Emergency-Session") String emergencySessionId,
            @Valid @RequestBody EmergencyChangeVaultPasswordRequest request) {
        return vault.emergencyChangePassword(emergencySessionId, request, current.family());
    }

    @GetMapping("/{memberId}/folders")
    public List<FolderResponse> folders(@PathVariable Long memberId, @RequestParam(defaultValue = "false") boolean emergency) {
        if (emergency) return vault.emergencyFolders(memberId, current.family());
        return vault.folders(memberId, current.member(), emergency);
    }

    @GetMapping("/{memberId}/files")
    public List<FileResponse> files(@PathVariable Long memberId, @RequestParam(defaultValue = "false") boolean emergency) {
        if (emergency) return vault.emergencyFiles(memberId, current.family());
        return vault.files(memberId, current.member(), emergency);
    }

    @PostMapping("/folders")
    public FolderResponse createFolder(@RequestBody FolderRequest request) {
        return vault.createFolder(request, current.member());
    }

    @PutMapping("/folder/{id}")
    public FolderResponse renameFolder(@PathVariable Long id, @RequestBody FolderRequest request) {
        return vault.renameFolder(id, request, current.member());
    }

    @DeleteMapping("/folder/{id}")
    public void deleteFolder(@PathVariable Long id) {
        vault.deleteFolder(id, current.member());
    }

    @PostMapping("/upload")
    public FileResponse upload(@RequestParam(required = false) Long folderId, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(defaultValue = "false") boolean hidden) {
        return vault.upload(folderId, file, hidden, current.member());
    }

    @DeleteMapping("/file/{id}")
    public void deleteFile(@PathVariable Long id) {
        vault.deleteFile(id, current.member());
    }

    @GetMapping("/{memberId}/file/{fileId}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long memberId, @PathVariable Long fileId, @RequestParam(defaultValue = "false") boolean emergency) {
        if (emergency) return vault.emergencyDownload(memberId, fileId, current.user(), current.family());
        return vault.download(memberId, fileId, current.member(), emergency);
    }
}

