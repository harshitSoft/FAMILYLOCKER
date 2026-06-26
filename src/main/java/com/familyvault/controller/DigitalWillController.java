package com.familyvault.controller;

import com.familyvault.dto.DigitalWillDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.DigitalWillService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vault")
public class DigitalWillController {
    private final DigitalWillService digitalWills;
    private final CurrentUserService current;

    public DigitalWillController(DigitalWillService digitalWills, CurrentUserService current) {
        this.digitalWills = digitalWills;
        this.current = current;
    }

    @GetMapping("/digital-will")
    public List<Response> list() {
        return digitalWills.list(current.member());
    }

    @PostMapping("/digital-will")
    public Response create(@Valid @RequestBody Request request) {
        return digitalWills.create(current.member(), request);
    }

    @PutMapping("/digital-will/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return digitalWills.update(current.member(), id, request);
    }

    @DeleteMapping("/digital-will/{id}")
    public void delete(@PathVariable Long id) {
        digitalWills.delete(current.member(), id);
    }

    @GetMapping("/emergency/{targetMemberId}/digital-will")
    public List<Response> emergencyList(@PathVariable Long targetMemberId,
                                        @RequestParam(required = false) String emergencySessionId,
                                        @RequestHeader(value = "X-Emergency-Session", required = false) String sessionHeader) {
        return digitalWills.emergencyList(current.family(), targetMemberId, sessionHeader != null ? sessionHeader : emergencySessionId);
    }
}
