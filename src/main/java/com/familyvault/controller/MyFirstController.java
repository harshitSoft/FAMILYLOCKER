package com.familyvault.controller;

import com.familyvault.dto.MyFirstDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.MyFirstService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vault")
public class MyFirstController {
    private final MyFirstService firsts;
    private final CurrentUserService current;

    public MyFirstController(MyFirstService firsts, CurrentUserService current) {
        this.firsts = firsts;
        this.current = current;
    }

    @GetMapping("/my-firsts")
    public List<Response> list() { return firsts.list(current.member()); }

    @PostMapping("/my-firsts")
    public Response create(@Valid @RequestBody Request request) { return firsts.create(current.member(), request); }

    @PutMapping("/my-firsts/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request request) {
        return firsts.update(current.member(), id, request);
    }

    @DeleteMapping("/my-firsts/{id}")
    public void delete(@PathVariable Long id) { firsts.delete(current.member(), id); }

    @GetMapping("/emergency/{targetMemberId}/my-firsts")
    public List<Response> emergencyList(@PathVariable Long targetMemberId,
                                        @RequestParam(required = false) String emergencySessionId,
                                        @RequestHeader(value = "X-Emergency-Session", required = false) String sessionHeader) {
        return firsts.emergencyList(current.family(), targetMemberId, sessionHeader != null ? sessionHeader : emergencySessionId);
    }
}
