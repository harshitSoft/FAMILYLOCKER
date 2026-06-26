package com.familyvault.controller;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.EmergencyService;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {
    private final EmergencyService emergency;
    private final CurrentUserService current;

    public EmergencyController(EmergencyService emergency, CurrentUserService current) {
        this.emergency = emergency;
        this.current = current;
    }

    @PostMapping("/request/{targetMemberId}")
    public EmergencyRequestResponse request(@PathVariable Long targetMemberId) {
        return emergency.request(targetMemberId, current.member());
    }

    @PostMapping("/approve/{requestId}")
    public EmergencyRequestResponse approve(@PathVariable Long requestId) {
        return emergency.approve(requestId, current.member());
    }

    @GetMapping("/unlocked/{requestId}/files")
    public List<FileResponse> files(@PathVariable Long requestId) {
        return emergency.unlockedFiles(requestId, current.member());
    }

    @GetMapping("/unlocked/{requestId}/files/{fileId}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long requestId, @PathVariable Long fileId) {
        return emergency.download(requestId, fileId, current.member());
    }
}
