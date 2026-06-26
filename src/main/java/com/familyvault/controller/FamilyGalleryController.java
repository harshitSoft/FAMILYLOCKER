package com.familyvault.controller;

import com.familyvault.dto.MediaDtos.FamilyGalleryResponse;
import com.familyvault.dto.MediaDtos.FamilyGalleryUploadResponse;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.FamilyGalleryService;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/family/gallery")
public class FamilyGalleryController {
    private final FamilyGalleryService gallery;
    private final CurrentUserService current;

    public FamilyGalleryController(FamilyGalleryService gallery, CurrentUserService current) {
        this.gallery = gallery;
        this.current = current;
    }

    @GetMapping
    public List<FamilyGalleryResponse> list() {
        return gallery.list(current.family());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FamilyGalleryUploadResponse upload(@RequestParam("title") String title,
                                              @RequestParam(value = "description", required = false) String description,
                                              @RequestParam("uploadedByName") String uploadedByName,
                                              @RequestParam("file") MultipartFile file) {
        return new FamilyGalleryUploadResponse(true, "Media uploaded successfully",
                gallery.upload(current.family(), title, description, uploadedByName, file));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<ByteArrayResource> preview(@PathVariable Long id) {
        return gallery.preview(current.family(), id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        return gallery.download(current.family(), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        gallery.delete(current.family(), id);
    }
}
