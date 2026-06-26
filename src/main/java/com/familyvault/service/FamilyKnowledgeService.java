package com.familyvault.service;

import com.familyvault.dto.FamilyKnowledgeDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyKnowledgeService {
    private final FamilyHistoryEntryRepository entries;
    private final FamilyImportantDateRepository dates;

    public FamilyKnowledgeService(FamilyHistoryEntryRepository entries, FamilyImportantDateRepository dates) {
        this.entries = entries;
        this.dates = dates;
    }

    @Transactional(readOnly = true)
    public List<HistoryEntryResponse> list(Family family, FamilyHistorySectionType type) {
        return entries.findByFamilyAndSectionTypeOrderByCreatedAtDesc(family, type).stream().map(this::toEntry).toList();
    }

    @Transactional
    public HistoryEntryResponse create(Family family, FamilyHistorySectionType type, HistoryEntryRequest request) {
        FamilyHistoryEntry entry = new FamilyHistoryEntry();
        entry.setFamily(family);
        entry.setSectionType(type);
        apply(entry, request);
        return toEntry(entries.save(entry));
    }

    @Transactional
    public HistoryEntryResponse update(Family family, FamilyHistorySectionType type, Long id, HistoryEntryRequest request) {
        FamilyHistoryEntry entry = entry(family, type, id);
        apply(entry, request);
        return toEntry(entry);
    }

    @Transactional
    public void delete(Family family, FamilyHistorySectionType type, Long id) {
        entries.delete(entry(family, type, id));
    }

    @Transactional(readOnly = true)
    public List<ImportantDateResponse> dates(Family family) {
        return dates.findByFamilyOrderByDateValueAsc(family).stream().map(this::toDate).toList();
    }

    @Transactional
    public ImportantDateResponse createDate(Family family, ImportantDateRequest request) {
        FamilyImportantDate date = new FamilyImportantDate();
        date.setFamily(family);
        applyDate(date, request);
        return toDate(dates.save(date));
    }

    @Transactional
    public ImportantDateResponse updateDate(Family family, Long id, ImportantDateRequest request) {
        FamilyImportantDate date = date(family, id);
        applyDate(date, request);
        return toDate(date);
    }

    @Transactional
    public void deleteDate(Family family, Long id) {
        dates.delete(date(family, id));
    }

    @Transactional(readOnly = true)
    public List<ImportantDateResponse> today(Family family) {
        MonthDay today = MonthDay.from(LocalDate.now());
        return dates.findByFamilyOrderByDateValueAsc(family).stream()
                .filter(date -> MonthDay.from(date.getDateValue()).equals(today))
                .map(this::toDate)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ImportantDateResponse> upcoming(Family family, int limit) {
        return dates.findByFamilyOrderByDateValueAsc(family).stream()
                .map(this::toDate)
                .sorted(Comparator.comparingInt(ImportantDateResponse::daysUntil))
                .limit(Math.max(1, limit))
                .toList();
    }

    private void apply(FamilyHistoryEntry entry, HistoryEntryRequest request) {
        if (request.contributorName() == null || request.contributorName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Contributor name is required");
        }
        entry.setTitle(request.title());
        entry.setMainText(request.mainText());
        entry.setFieldOne(request.fieldOne());
        entry.setFieldTwo(request.fieldTwo());
        entry.setFieldThree(request.fieldThree());
        entry.setFieldFour(request.fieldFour());
        entry.setFieldFive(request.fieldFive());
        entry.setContributorName(request.contributorName().trim());
        entry.setContributorRelation(blankToNull(request.contributorRelation()));
    }

    private void applyDate(FamilyImportantDate date, ImportantDateRequest request) {
        if (request.contributorName() == null || request.contributorName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Contributor name is required");
        }
        date.setTitle(request.title().trim());
        date.setPersonName(blankToNull(request.personName()));
        date.setDateValue(request.date());
        date.setCategory(request.category() == null ? ImportantDateCategory.CUSTOM : request.category());
        date.setDescription(blankToNull(request.description()));
        date.setContributorName(request.contributorName().trim());
        date.setContributorRelation(blankToNull(request.contributorRelation()));
    }

    private FamilyHistoryEntry entry(Family family, FamilyHistorySectionType type, Long id) {
        return entries.findByIdAndFamilyAndSectionType(id, family, type)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "History entry not found"));
    }

    private FamilyImportantDate date(Family family, Long id) {
        return dates.findByIdAndFamily(id, family)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Important date not found"));
    }

    private HistoryEntryResponse toEntry(FamilyHistoryEntry entry) {
        return new HistoryEntryResponse(entry.getId(), entry.getSectionType(), entry.getTitle(), entry.getMainText(),
                entry.getFieldOne(), entry.getFieldTwo(), entry.getFieldThree(), entry.getFieldFour(), entry.getFieldFive(),
                entry.getContributorName(), entry.getContributorRelation(), entry.getCreatedAt(), entry.getUpdatedAt());
    }

    private ImportantDateResponse toDate(FamilyImportantDate date) {
        return new ImportantDateResponse(date.getId(), date.getTitle(), date.getPersonName(), date.getDateValue(),
                date.getCategory(), date.getDescription(), date.getContributorName(), date.getContributorRelation(),
                date.getCreatedAt(), date.getUpdatedAt(), daysUntil(date.getDateValue()));
    }

    private int daysUntil(LocalDate value) {
        LocalDate today = LocalDate.now();
        LocalDate next = value.withYear(today.getYear());
        if (next.isBefore(today)) next = next.plusYears(1);
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, next);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
