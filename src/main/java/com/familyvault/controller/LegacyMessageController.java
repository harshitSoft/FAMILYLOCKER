package com.familyvault.controller;

import com.familyvault.dto.LegacyMessageDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.LegacyMessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vault")
public class LegacyMessageController {
    private final LegacyMessageService legacyMessages;
    private final CurrentUserService current;

    public LegacyMessageController(LegacyMessageService legacyMessages, CurrentUserService current) {
        this.legacyMessages = legacyMessages;
        this.current = current;
    }

    @GetMapping("/legacy-messages")
    public List<Response> list() {
        return legacyMessages.list(current.member());
    }

    @PostMapping("/legacy-messages")
    public Response create(@Valid @RequestBody Request request) {
        return legacyMessages.create(current.member(), request);
    }

    @PutMapping("/legacy-messages/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return legacyMessages.update(current.member(), id, request);
    }

    @DeleteMapping("/legacy-messages/{id}")
    public void delete(@PathVariable Long id) {
        legacyMessages.delete(current.member(), id);
    }

    @GetMapping("/emergency/{targetMemberId}/legacy-messages")
    public List<Response> emergencyList(@PathVariable Long targetMemberId,
                                        @RequestParam(required = false) String emergencySessionId,
                                        @RequestHeader(value = "X-Emergency-Session", required = false) String sessionHeader) {
        return legacyMessages.emergencyList(current.family(), targetMemberId, sessionHeader != null ? sessionHeader : emergencySessionId);
    }
}
