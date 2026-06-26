package com.familyvault.controller;

import com.familyvault.dto.FamilyKnowledgeDto.*;
import com.familyvault.entity.FamilyHistorySectionType;
import com.familyvault.security.CurrentUserService;
import com.familyvault.service.FamilyKnowledgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family/history")
public class FamilyKnowledgeController {
    private final FamilyKnowledgeService knowledge;
    private final CurrentUserService current;

    public FamilyKnowledgeController(FamilyKnowledgeService knowledge, CurrentUserService current) {
        this.knowledge = knowledge;
        this.current = current;
    }

    @GetMapping("/origin")
    public List<HistoryEntryResponse> origin() { return list(FamilyHistorySectionType.ORIGIN); }
    @PostMapping("/origin")
    public HistoryEntryResponse createOrigin(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.ORIGIN, request); }
    @PutMapping("/origin/{id}")
    public HistoryEntryResponse updateOrigin(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.ORIGIN, id, request); }
    @DeleteMapping("/origin/{id}")
    public void deleteOrigin(@PathVariable Long id) { delete(FamilyHistorySectionType.ORIGIN, id); }

    @GetMapping("/ancestors")
    public List<HistoryEntryResponse> ancestors() { return list(FamilyHistorySectionType.ANCESTOR); }
    @PostMapping("/ancestors")
    public HistoryEntryResponse createAncestor(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.ANCESTOR, request); }
    @PutMapping("/ancestors/{id}")
    public HistoryEntryResponse updateAncestor(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.ANCESTOR, id, request); }
    @DeleteMapping("/ancestors/{id}")
    public void deleteAncestor(@PathVariable Long id) { delete(FamilyHistorySectionType.ANCESTOR, id); }

    @GetMapping("/past")
    public List<HistoryEntryResponse> past() { return list(FamilyHistorySectionType.FAMILY_PAST); }
    @PostMapping("/past")
    public HistoryEntryResponse createPast(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.FAMILY_PAST, request); }
    @PutMapping("/past/{id}")
    public HistoryEntryResponse updatePast(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.FAMILY_PAST, id, request); }
    @DeleteMapping("/past/{id}")
    public void deletePast(@PathVariable Long id) { delete(FamilyHistorySectionType.FAMILY_PAST, id); }

    @GetMapping("/kuldevta")
    public List<HistoryEntryResponse> kuldevta() { return list(FamilyHistorySectionType.KULDEVTA); }
    @PostMapping("/kuldevta")
    public HistoryEntryResponse createKuldevta(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.KULDEVTA, request); }
    @PutMapping("/kuldevta/{id}")
    public HistoryEntryResponse updateKuldevta(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.KULDEVTA, id, request); }
    @DeleteMapping("/kuldevta/{id}")
    public void deleteKuldevta(@PathVariable Long id) { delete(FamilyHistorySectionType.KULDEVTA, id); }

    @GetMapping("/rituals")
    public List<HistoryEntryResponse> rituals() { return list(FamilyHistorySectionType.RITUAL); }
    @PostMapping("/rituals")
    public HistoryEntryResponse createRitual(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.RITUAL, request); }
    @PutMapping("/rituals/{id}")
    public HistoryEntryResponse updateRitual(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.RITUAL, id, request); }
    @DeleteMapping("/rituals/{id}")
    public void deleteRitual(@PathVariable Long id) { delete(FamilyHistorySectionType.RITUAL, id); }

    @GetMapping("/festivals")
    public List<HistoryEntryResponse> festivals() { return list(FamilyHistorySectionType.FESTIVAL); }
    @PostMapping("/festivals")
    public HistoryEntryResponse createFestival(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.FESTIVAL, request); }
    @PutMapping("/festivals/{id}")
    public HistoryEntryResponse updateFestival(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.FESTIVAL, id, request); }
    @DeleteMapping("/festivals/{id}")
    public void deleteFestival(@PathVariable Long id) { delete(FamilyHistorySectionType.FESTIVAL, id); }

    @GetMapping("/health")
    public List<HistoryEntryResponse> health() { return list(FamilyHistorySectionType.HEALTH); }
    @PostMapping("/health")
    public HistoryEntryResponse createHealth(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.HEALTH, request); }
    @PutMapping("/health/{id}")
    public HistoryEntryResponse updateHealth(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.HEALTH, id, request); }
    @DeleteMapping("/health/{id}")
    public void deleteHealth(@PathVariable Long id) { delete(FamilyHistorySectionType.HEALTH, id); }

    @GetMapping("/blood-groups")
    public List<HistoryEntryResponse> bloodGroups() { return list(FamilyHistorySectionType.BLOOD_GROUP); }
    @PostMapping("/blood-groups")
    public HistoryEntryResponse createBloodGroup(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.BLOOD_GROUP, request); }
    @PutMapping("/blood-groups/{id}")
    public HistoryEntryResponse updateBloodGroup(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.BLOOD_GROUP, id, request); }
    @DeleteMapping("/blood-groups/{id}")
    public void deleteBloodGroup(@PathVariable Long id) { delete(FamilyHistorySectionType.BLOOD_GROUP, id); }

    @GetMapping("/kundali")
    public List<HistoryEntryResponse> kundali() { return list(FamilyHistorySectionType.KUNDALI); }
    @PostMapping("/kundali")
    public HistoryEntryResponse createKundali(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.KUNDALI, request); }
    @PutMapping("/kundali/{id}")
    public HistoryEntryResponse updateKundali(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.KUNDALI, id, request); }
    @DeleteMapping("/kundali/{id}")
    public void deleteKundali(@PathVariable Long id) { delete(FamilyHistorySectionType.KUNDALI, id); }

    @GetMapping("/custom-notes")
    public List<HistoryEntryResponse> customNotes() { return list(FamilyHistorySectionType.CUSTOM_NOTE); }
    @PostMapping("/custom-notes")
    public HistoryEntryResponse createCustomNote(@Valid @RequestBody HistoryEntryRequest request) { return create(FamilyHistorySectionType.CUSTOM_NOTE, request); }
    @PutMapping("/custom-notes/{id}")
    public HistoryEntryResponse updateCustomNote(@PathVariable Long id, @Valid @RequestBody HistoryEntryRequest request) { return update(FamilyHistorySectionType.CUSTOM_NOTE, id, request); }
    @DeleteMapping("/custom-notes/{id}")
    public void deleteCustomNote(@PathVariable Long id) { delete(FamilyHistorySectionType.CUSTOM_NOTE, id); }

    @GetMapping("/important-dates")
    public List<ImportantDateResponse> importantDates() { return knowledge.dates(current.family()); }
    @PostMapping("/important-dates")
    public ImportantDateResponse createImportantDate(@Valid @RequestBody ImportantDateRequest request) { return knowledge.createDate(current.family(), request); }
    @PutMapping("/important-dates/{id}")
    public ImportantDateResponse updateImportantDate(@PathVariable Long id, @Valid @RequestBody ImportantDateRequest request) { return knowledge.updateDate(current.family(), id, request); }
    @DeleteMapping("/important-dates/{id}")
    public void deleteImportantDate(@PathVariable Long id) { knowledge.deleteDate(current.family(), id); }
    @GetMapping("/important-dates/today")
    public List<ImportantDateResponse> today() { return knowledge.today(current.family()); }
    @GetMapping("/important-dates/upcoming")
    public List<ImportantDateResponse> upcoming(@RequestParam(defaultValue = "5") int limit) { return knowledge.upcoming(current.family(), limit); }

    private List<HistoryEntryResponse> list(FamilyHistorySectionType type) { return knowledge.list(current.family(), type); }
    private HistoryEntryResponse create(FamilyHistorySectionType type, HistoryEntryRequest request) { return knowledge.create(current.family(), type, request); }
    private HistoryEntryResponse update(FamilyHistorySectionType type, Long id, HistoryEntryRequest request) { return knowledge.update(current.family(), type, id, request); }
    private void delete(FamilyHistorySectionType type, Long id) { knowledge.delete(current.family(), type, id); }
}
