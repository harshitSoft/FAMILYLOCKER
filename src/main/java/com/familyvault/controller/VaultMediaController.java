package com.familyvault.controller;

import com.familyvault.dto.MediaDtos.VaultMediaResponse;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.VaultMediaService;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vault/media")
public class VaultMediaController {
    private final VaultMediaService media;
    private final CurrentUserService current;

    public VaultMediaController(VaultMediaService media, CurrentUserService current) {
        this.media = media;
        this.current = current;
    }

    @GetMapping
    public List<VaultMediaResponse> list(@RequestParam(required = false) Long memberId,
                                         @RequestParam(defaultValue = "false") boolean emergency) {
        Long ownerId = memberId == null ? current.member().getId() : memberId;
        if (emergency) return media.emergencyList(ownerId, current.family());
        return media.list(ownerId, current.member(), false);
    }

    @PostMapping("/upload")
    public VaultMediaResponse upload(@RequestParam String title,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(defaultValue = "false") boolean privateHidden,
                                     @RequestParam("file") MultipartFile file) {
        return media.upload(current.member(), title, description, privateHidden, file);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<ByteArrayResource> preview(@PathVariable Long id,
                                                     @RequestParam(required = false) Long memberId,
                                                     @RequestParam(defaultValue = "false") boolean emergency) {
        Long ownerId = memberId == null ? current.member().getId() : memberId;
        if (emergency) return media.emergencyPreview(ownerId, id, current.family());
        return media.preview(ownerId, id, current.member(), false);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id,
                                                      @RequestParam(required = false) Long memberId,
                                                      @RequestParam(defaultValue = "false") boolean emergency) {
        Long ownerId = memberId == null ? current.member().getId() : memberId;
        if (emergency) return media.emergencyDownload(ownerId, id, current.family());
        return media.download(ownerId, id, current.member(), false);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        media.delete(id, current.member());
    }
}
