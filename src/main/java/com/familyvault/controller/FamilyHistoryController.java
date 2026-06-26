package com.familyvault.controller;

import com.familyvault.dto.FamilyHistoryDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.FamilyHistoryService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyHistoryController {
    private final FamilyHistoryService history;
    private final CurrentUserService current;

    public FamilyHistoryController(FamilyHistoryService history, CurrentUserService current) {
        this.history = history;
        this.current = current;
    }

    @GetMapping("/history")
    public HistoryResponse getHistory() { return history.get(current.family()); }

    @PutMapping("/history")
    public HistoryResponse saveHistory(@RequestBody HistoryRequest request) { return history.save(current.family(), request); }

    @GetMapping("/relations")
    public List<RelationResponse> relations() { return history.relations(current.family()); }

    @PostMapping("/relations")
    public RelationResponse createRelation(@RequestBody RelationRequest request) { return history.createRelation(current.family(), request); }

    @PutMapping("/relations/{id}")
    public RelationResponse updateRelation(@PathVariable Long id, @RequestBody RelationRequest request) {
        return history.updateRelation(current.family(), id, request);
    }

    @DeleteMapping("/relations/{id}")
    public void deleteRelation(@PathVariable Long id) { history.deleteRelation(current.family(), id); }
}
