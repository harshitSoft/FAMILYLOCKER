package com.familyvault.service;

import com.familyvault.dto.FamilyAiGuideDto.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.FamilyKnowledgeContributorScoreRepository;
import com.familyvault.repository.FamilyKnowledgeEntryRepository;
import com.familyvault.repository.FamilyMemberRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyAiGuideService {
    private final FamilyKnowledgeEntryRepository knowledge;
    private final FamilyKnowledgeContributorScoreRepository scores;
    private final FamilyMemberRepository members;

    public FamilyAiGuideService(FamilyKnowledgeEntryRepository knowledge,
                                FamilyKnowledgeContributorScoreRepository scores,
                                FamilyMemberRepository members) {
        this.knowledge = knowledge;
        this.scores = scores;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(Family family, String query) {
        String cleanQuery = normalize(query);
        if (cleanQuery.isBlank()) {
            return new SearchResponse(query, "Ask a question to search your family wisdom.", List.of());
        }
        Set<String> terms = expandTerms(cleanQuery);
        List<SearchResult> results = knowledge.findByFamilyOrderByCreatedAtDesc(family).stream()
                .map(entry -> toResult(entry, terms))
                .filter(result -> result.score() >= 50)
                .sorted(Comparator.comparingInt(SearchResult::score).reversed()
                        .thenComparing(SearchResult::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        String summary = results.isEmpty()
                ? "Sorry, I don't know about this yet. You can share this knowledge with your family."
                : "I found " + results.size() + " family wisdom record" + (results.size() == 1 ? "" : "s") + ".";
        return new SearchResponse(query, summary, results);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeResponse> list(Family family) {
        return knowledge.findByFamilyOrderByCreatedAtDesc(family).stream().map(this::toKnowledge).toList();
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> ranking(Family family) {
        return scores.findByFamilyOrderByTotalPointsDescTotalContributionsDescUpdatedAtDesc(family).stream()
                .map(score -> new RankingResponse(score.getId(), score.getContributorMemberId(), score.getContributorName(),
                        score.getTotalPoints(), score.getTotalContributions(), score.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> categories(Family family) {
        return knowledge.findByFamilyOrderByCreatedAtDesc(family).stream()
                .map(entry -> clean(entry.getCategory()))
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> types(Family family, String category) {
        String wantedCategory = normalize(category);
        return knowledge.findByFamilyOrderByCreatedAtDesc(family).stream()
                .filter(entry -> normalize(entry.getCategory()).equals(wantedCategory))
                .map(entry -> clean(entry.getType()))
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GuideTitleResponse> titles(Family family, String category, String type) {
        String wantedCategory = normalize(category);
        String wantedType = normalize(type);
        return knowledge.findByFamilyOrderByCreatedAtDesc(family).stream()
                .filter(entry -> normalize(entry.getCategory()).equals(wantedCategory))
                .filter(entry -> normalize(entry.getType()).equals(wantedType))
                .filter(entry -> !clean(entry.getTitle()).isBlank())
                .sorted(Comparator.comparing(FamilyKnowledgeEntry::getTitle, String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new GuideTitleResponse(entry.getId(), entry.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GuideAnswerResponse answer(Family family, Long id, String category, String type, String title) {
        FamilyKnowledgeEntry entry;
        if (id != null) {
            entry = knowledge.findByIdAndFamily(id, family)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Family wisdom not found"));
        } else {
            String wantedCategory = normalize(category);
            String wantedType = normalize(type);
            String wantedTitle = normalize(title);
            entry = knowledge.findByFamilyOrderByCreatedAtDesc(family).stream()
                    .filter(item -> normalize(item.getCategory()).equals(wantedCategory))
                    .filter(item -> normalize(item.getType()).equals(wantedType))
                    .filter(item -> normalize(item.getTitle()).equals(wantedTitle))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Family wisdom not found"));
        }
        return new GuideAnswerResponse(entry.getId(), entry.getCategory(), entry.getType(), entry.getTitle(),
                entry.getExplanation(), entry.getContributorName(), entry.getCreatedAt());
    }

    @Transactional
    public KnowledgeSaveResponse create(Family family, KnowledgeRequest request) {
        FamilyKnowledgeEntry entry = new FamilyKnowledgeEntry();
        entry.setFamily(family);
        entry.setSourceType(FamilyKnowledgeSourceType.MANUAL);
        apply(entry, request);
        KnowledgeResponse saved = toKnowledge(knowledge.save(entry));
        addPoint(family, entry.getContributorMemberId(), entry.getContributorName());
        return new KnowledgeSaveResponse(true, "Family knowledge saved successfully", 1, saved);
    }

    @Transactional
    public KnowledgeResponse update(Family family, Long id, KnowledgeRequest request) {
        FamilyKnowledgeEntry entry = knowledge.findByIdAndFamily(id, family)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Knowledge entry not found"));
        apply(entry, request);
        return toKnowledge(entry);
    }

    @Transactional
    public void delete(Family family, Long id) {
        FamilyKnowledgeEntry entry = knowledge.findByIdAndFamily(id, family)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Knowledge entry not found"));
        knowledge.delete(entry);
    }

    private void apply(FamilyKnowledgeEntry entry, KnowledgeRequest request) {
        FamilyMember contributor = null;
        if (request.contributorMemberId() != null) {
            contributor = members.findByIdAndFamily(request.contributorMemberId(), entry.getFamily())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Contributor member is invalid"));
        }
        entry.setContributorMemberId(contributor == null ? null : contributor.getId());
        entry.setContributorName(required(firstNotBlank(contributor == null ? null : contributor.getFullName(), request.contributorName()), "Contributor name"));
        entry.setCategory(required(request.category(), "Category"));
        entry.setType(required(request.type(), "Type"));
        entry.setTitle(required(request.title(), "Title"));
        entry.setExplanation(required(request.explanation(), "Explanation"));
        entry.setDescription(firstNotBlank(request.description(), request.type()));
        entry.setKeywords(firstNotBlank(request.keywords(), String.join(", ", entry.getCategory(), entry.getType(), entry.getTitle())));
    }

    private void addPoint(Family family, Long contributorMemberId, String contributorName) {
        FamilyKnowledgeContributorScore score;
        if (contributorMemberId != null) {
            score = scores.findByFamilyAndContributorMemberId(family, contributorMemberId).orElseGet(() -> newScore(family, contributorMemberId));
        } else {
            score = scores.findByFamilyAndContributorNameIgnoreCase(family, contributorName).orElseGet(() -> newScore(family, null));
        }
        score.setContributorName(contributorName);
        score.setTotalPoints(score.getTotalPoints() + 1);
        score.setTotalContributions(score.getTotalContributions() + 1);
        scores.save(score);
    }

    private FamilyKnowledgeContributorScore newScore(Family family, Long contributorMemberId) {
        FamilyKnowledgeContributorScore next = new FamilyKnowledgeContributorScore();
        next.setFamily(family);
        next.setContributorMemberId(contributorMemberId);
        return next;
    }

    private SearchResult toResult(FamilyKnowledgeEntry entry, Set<String> terms) {
        SearchScore score = score(entry, terms);
        return new SearchResult(entry.getTitle(), entry.getDescription(), entry.getExplanation(), entry.getCategory(),
                FamilyKnowledgeSourceType.MANUAL, "Family Wisdom", entry.getContributorName(),
                entry.getCreatedAt(), score.total(), score.matches());
    }

    private SearchScore score(FamilyKnowledgeEntry entry, Set<String> terms) {
        List<String> matches = new ArrayList<>();
        int total = 0;
        String title = normalizeSearch(entry.getTitle());
        String keywords = normalizeSearch(entry.getKeywords());
        String category = normalizeSearch(entry.getCategory());
        String query = terms.stream().findFirst().orElse("");
        if (!query.isBlank() && title.equals(query)) {
            total += 100;
            matches.add(entry.getTitle());
        }
        if (!query.isBlank() && keywordPhrases(keywords).stream().anyMatch(keyword -> keyword.equals(query))) {
            total += 90;
            matches.add(query);
        }
        total += titleTokenScore(title, terms, matches);
        total += keywordTokenScore(keywords, terms, matches);
        if (terms.contains(category)) {
            total += 10;
            matches.add(category);
        }
        if (onlyWeakMatches(matches)) total = 0;
        return new SearchScore(total, matches.stream().distinct().limit(8).toList());
    }

    private int titleTokenScore(String title, Set<String> terms, List<String> matches) {
        if (title.isBlank()) return 0;
        int score = 0;
        for (String term : terms) {
            if (term.isBlank()) continue;
            if (titleTokens(title).contains(term)) {
                score += 25;
                matches.add(term);
            }
        }
        return score;
    }

    private int keywordTokenScore(String keywords, Set<String> terms, List<String> matches) {
        if (keywords.isBlank()) return 0;
        int score = 0;
        List<String> phrases = keywordPhrases(keywords);
        for (String term : terms) {
            if (term.isBlank()) continue;
            for (String keyword : phrases) {
                if (keyword.equals(term)) {
                    score += 30;
                    matches.add(keyword);
                } else if (keyword.contains(" ") && keyword.contains(term)) {
                    score += 30;
                    matches.add(keyword);
                }
            }
        }
        return score;
    }

    private KnowledgeResponse toKnowledge(FamilyKnowledgeEntry entry) {
        return new KnowledgeResponse(entry.getId(), entry.getFamilyId(), entry.getContributorMemberId(), entry.getContributorName(),
                entry.getCategory(), entry.getType(), entry.getTitle(), entry.getExplanation(), entry.getDescription(), entry.getKeywords(),
                entry.getSourceType(), entry.getSourceId(), entry.getCreatedAt(), entry.getUpdatedAt());
    }

    private Set<String> expandTerms(String query) {
        String cleanQuery = normalizeSearch(query);
        Set<String> stopWords = Set.of("what", "how", "is", "are", "the", "a", "an", "in", "on", "of", "for", "to", "me", "my", "about", "tell", "know",
                "hum", "kya", "kaise", "kese", "hai", "h", "mein", "ka", "ki", "ke", "kon", "kaun", "batao", "ghar");
        Map<String, List<String>> synonyms = Map.ofEntries(
                Map.entry("chitragupt", List.of("chitragupta", "चित्रगुप्त")),
                Map.entry("chitragupta", List.of("chitragupt", "चित्रगुप्त")),
                Map.entry("diwali", List.of("deepawali", "dipawali", "दिवाली", "दीपावली")),
                Map.entry("deepawali", List.of("diwali", "dipawali", "दिवाली", "दीपावली")),
                Map.entry("pooja", List.of("puja", "पूजा")),
                Map.entry("puja", List.of("pooja", "पूजा")),
                Map.entry("pen", List.of("kalam", "lekhani", "कलम")),
                Map.entry("kalam", List.of("pen", "lekhani", "कलम")),
                Map.entry("holi", List.of("रंगपंचमी", "festival")),
                Map.entry("riti rivaj", List.of("riwaj", "ritual", "tradition", "parampara")),
                Map.entry("riwaj", List.of("riti rivaj", "ritual", "tradition", "parampara")),
                Map.entry("kuldevta", List.of("kuldevi", "deity")),
                Map.entry("kuldevi", List.of("kuldevta", "deity")),
                Map.entry("salah", List.of("advice", "guidance")),
                Map.entry("kahani", List.of("story")),
                Map.entry("anubhav", List.of("experience"))
        );
        Set<String> terms = new LinkedHashSet<>();
        terms.add(cleanQuery);
        Arrays.stream(cleanQuery.split("\\s+"))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .filter(term -> !stopWords.contains(term))
                .forEach(terms::add);
        synonyms.forEach((key, values) -> {
            boolean related = cleanQuery.contains(key) || compact(cleanQuery).contains(compact(key))
                    || values.stream().anyMatch(value -> cleanQuery.contains(normalizeSearch(value)) || compact(cleanQuery).contains(compact(value)));
            if (related) {
                terms.add(key);
                values.forEach(value -> terms.add(normalizeSearch(value)));
            }
        });
        return terms;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private String compact(String value) {
        return normalize(value).replace(" ", "");
    }

    private String normalizeSearch(String value) {
        return normalize(value).replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private List<String> keywordPhrases(String keywords) {
        return Arrays.stream(keywords.split(",")).map(this::normalizeSearch).filter(keyword -> !keyword.isBlank()).toList();
    }

    private Set<String> titleTokens(String title) {
        return new LinkedHashSet<>(Arrays.asList(title.split("\\s+")));
    }

    private boolean onlyWeakMatches(List<String> matches) {
        if (matches.isEmpty()) return false;
        Set<String> weak = Set.of("ghar", "family", "member", "pooja", "festival", "ritual", "story", "history", "diwali");
        return matches.stream().map(this::normalizeSearch).allMatch(weak::contains);
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, label + " is required");
        return value.trim();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNotBlank(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : second;
    }

    private record SearchScore(int total, List<String> matches) {}
}
