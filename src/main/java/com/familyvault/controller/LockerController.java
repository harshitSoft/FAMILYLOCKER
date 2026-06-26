package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.entity.FamilyMember;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.LockerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/locker/my")
public class LockerController {
    private final CurrentUserService current;
    private final LockerService locker;

    public LockerController(CurrentUserService current, LockerService locker) {
        this.current = current;
        this.locker = locker;
    }

    @GetMapping
    public Map<String, Object> myLocker() {
        FamilyMember member = current.member();
        return Map.of("lockerId", member.getLocker().getId(), "owner", member.getFullName(), "memberId", member.getMemberCode(), "familyId", member.getFamily().getFamilyCode());
    }

    @GetMapping("/folders")
    public List<FolderResponse> folders() {
        return locker.folders(current.member());
    }

    @PostMapping("/folders")
    public FolderResponse createFolder(@Valid @RequestBody FolderRequest request) {
        return locker.createFolder(current.member(), request);
    }

    @PutMapping("/folders/{folderId}")
    public FolderResponse updateFolder(@PathVariable Long folderId, @Valid @RequestBody FolderRequest request) {
        return locker.updateFolder(current.member(), folderId, request);
    }

    @DeleteMapping("/folders/{folderId}")
    public void deleteFolder(@PathVariable Long folderId) {
        locker.deleteFolder(current.member(), folderId);
    }

    @PostMapping("/folders/{folderId}/files")
    public FileResponse upload(@PathVariable Long folderId, @RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "false") boolean hidden) {
        return locker.upload(current.member(), folderId, file, hidden);
    }

    @GetMapping("/files")
    public List<FileResponse> files() {
        return locker.list(current.member());
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long fileId) {
        FamilyMember member = current.member();
        return locker.download(member, fileId, false, member.getUser());
    }

    @PutMapping("/files/{fileId}")
    public FileResponse updateFile(@PathVariable Long fileId, @RequestBody FileUpdateRequest request) {
        return locker.updateFile(current.member(), fileId, request);
    }

    @DeleteMapping("/files/{fileId}")
    public void deleteFile(@PathVariable Long fileId) {
        locker.deleteFile(current.member(), fileId);
    }
}
