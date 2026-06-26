package com.familyvault.controller;

import com.familyvault.dto.FamilyAiGuideDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.FamilyAiGuideService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family/ask-virasat")
public class FamilyAiGuideController {
    private final FamilyAiGuideService guide;
    private final CurrentUserService current;

    public FamilyAiGuideController(FamilyAiGuideService guide, CurrentUserService current) {
        this.guide = guide;
        this.current = current;
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query) {
        return guide.search(current.family(), query);
    }

    @GetMapping("/knowledge")
    public List<KnowledgeResponse> knowledge() {
        return guide.list(current.family());
    }

    @GetMapping("/ranking")
    public List<RankingResponse> ranking() {
        return guide.ranking(current.family());
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return guide.categories(current.family());
    }

    @GetMapping("/types")
    public List<String> types(@RequestParam String category) {
        return guide.types(current.family(), category);
    }

    @GetMapping("/titles")
    public List<GuideTitleResponse> titles(@RequestParam String category, @RequestParam String type) {
        return guide.titles(current.family(), category, type);
    }

    @GetMapping("/answer")
    public GuideAnswerResponse answer(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String title) {
        return guide.answer(current.family(), id, category, type, title);
    }

    @PostMapping("/knowledge")
    public KnowledgeSaveResponse create(@Valid @RequestBody KnowledgeRequest request) {
        return guide.create(current.family(), request);
    }

    @PutMapping("/knowledge/{id}")
    public KnowledgeResponse update(@PathVariable Long id, @Valid @RequestBody KnowledgeRequest request) {
        return guide.update(current.family(), id, request);
    }

    @DeleteMapping("/knowledge/{id}")
    public void delete(@PathVariable Long id) {
        guide.delete(current.family(), id);
    }
}
