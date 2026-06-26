package com.familyvault.controller;

import com.familyvault.dto.RelationshipTreeDto.*;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.RelationshipTreeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyTreeController {
    private final RelationshipTreeService tree;
    private final CurrentUserService current;

    public FamilyTreeController(RelationshipTreeService tree, CurrentUserService current) {
        this.tree = tree;
        this.current = current;
    }

    @GetMapping("/tree-relations")
    public TreeResponse tree() {
        return tree.tree(current.family());
    }

    @PostMapping("/tree-person/root")
    public PersonResponse createRoot(@Valid @RequestBody PersonRequest request) {
        return tree.createRoot(current.family(), request);
    }

    @PostMapping("/tree-person/{selectedPersonId}/parent")
    public PersonResponse addParent(@PathVariable Long selectedPersonId, @Valid @RequestBody PersonRequest request) {
        return tree.addParent(current.family(), selectedPersonId, request);
    }

    @PostMapping("/tree-person/{selectedPersonId}/spouse")
    public PersonResponse addSpouse(@PathVariable Long selectedPersonId, @Valid @RequestBody PersonRequest request) {
        return tree.addSpouse(current.family(), selectedPersonId, request);
    }

    @PostMapping("/tree-person/{selectedPersonId}/child")
    public PersonResponse addChild(@PathVariable Long selectedPersonId, @Valid @RequestBody PersonRequest request) {
        return tree.addChild(current.family(), selectedPersonId, request);
    }

    @PutMapping("/tree-person/{personId}")
    public PersonResponse update(@PathVariable Long personId, @Valid @RequestBody PersonRequest request) {
        return tree.update(current.family(), personId, request);
    }

    @DeleteMapping("/tree-person/{personId}")
    public void delete(@PathVariable Long personId) {
        tree.delete(current.family(), personId);
    }

    @GetMapping("/tree-person/search")
    public List<PersonResponse> search(@RequestParam(defaultValue = "") String query) {
        return tree.search(current.family(), query);
    }
}
