package com.familyvault.controller;

import com.familyvault.dto.LegendDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.LegendService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family/legends")
public class LegendController {
    private final LegendService legends;
    private final CurrentUserService current;

    public LegendController(LegendService legends, CurrentUserService current) {
        this.legends = legends;
        this.current = current;
    }

    @GetMapping
    public List<LockerResponse> list() { return legends.list(current.family()); }

    @PostMapping
    public LockerResponse create(@Valid @RequestBody LockerRequest request) { return legends.create(current.family(), request); }

    @GetMapping("/{legendId}")
    public LockerResponse get(@PathVariable Long legendId) { return legends.get(current.family(), legendId); }

    @PutMapping("/{legendId}")
    public LockerResponse update(@PathVariable Long legendId, @Valid @RequestBody LockerRequest request) {
        return legends.update(current.family(), legendId, request);
    }

    @DeleteMapping("/{legendId}")
    public void delete(@PathVariable Long legendId) { legends.delete(current.family(), legendId); }

    @GetMapping("/{legendId}/memories")
    public List<MemoryResponse> memories(@PathVariable Long legendId) { return legends.memories(current.family(), legendId); }

    @PostMapping("/{legendId}/memories")
    public MemoryResponse createMemory(@PathVariable Long legendId, @Valid @RequestBody MemoryRequest request) {
        return legends.createMemory(current.family(), legendId, request);
    }

    @PutMapping("/{legendId}/memories/{memoryId}")
    public MemoryResponse updateMemory(@PathVariable Long legendId, @PathVariable Long memoryId, @Valid @RequestBody MemoryRequest request) {
        return legends.updateMemory(current.family(), legendId, memoryId, request);
    }

    @DeleteMapping("/{legendId}/memories/{memoryId}")
    public void deleteMemory(@PathVariable Long legendId, @PathVariable Long memoryId) {
        legends.deleteMemory(current.family(), legendId, memoryId);
    }
}
