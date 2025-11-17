package org.ohdsi.webapi.mvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ConceptSetOptimizationResult;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.cscompare.CompareArbitraryDto;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.vocabulary.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC version of VocabularyService
 *
 * Migration Status: Replaces /service/VocabularyService.java (Jersey)
 * Endpoints: 40+ endpoints for vocabulary operations
 * Complexity: High - complex search, lookups, concept set operations
 *
 * Note: This controller delegates to the existing Jersey VocabularyService to reuse
 * all business logic while providing Spring MVC endpoints.
 */
@RestController
@RequestMapping("/vocabulary")
public class VocabularyMvcController extends AbstractMvcController {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * DTO for ancestor/descendant concept ID lists
     */
    public static class ConceptIdListsDto {
        public List<Long> ancestors;
        public List<Long> descendants;
    }

    /**
     * Calculates the full set of ancestor and descendant concepts for a list of
     * ancestor and descendant concepts specified.
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/lookup/identifiers/ancestors
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/lookup/identifiers/ancestors
     */
    @PostMapping(value = "/{sourceKey}/lookup/identifiers/ancestors",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Long, List<Long>>> calculateAscendants(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptIdListsDto dto) throws Exception {
        // Convert our DTO to the private Ids class using reflection and JSON serialization
        String json = objectMapper.writeValueAsString(dto);
        Class<?> idsClass = Class.forName("org.ohdsi.webapi.service.VocabularyService$Ids");
        Object ids = objectMapper.readValue(json, idsClass);

        // Use reflection to call the method since Ids is private
        java.lang.reflect.Method method = VocabularyService.class.getMethod(
            "calculateAscendants", String.class, idsClass);
        @SuppressWarnings("unchecked")
        Map<Long, List<Long>> result = (Map<Long, List<Long>>) method.invoke(
            vocabularyService, sourceKey, ids);
        return ok(result);
    }

    /**
     * Get concepts from concept identifiers (IDs) from a specific source
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/lookup/identifiers
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/lookup/identifiers
     */
    @PostMapping(value = "/{sourceKey}/lookup/identifiers",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeIdentifierLookup(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody long[] identifiers) {
        Collection<Concept> concepts = vocabularyService.executeIdentifierLookup(sourceKey, identifiers);
        return ok(concepts);
    }

    /**
     * Get concepts from concept identifiers (IDs) from the default vocabulary source
     *
     * Jersey: POST /WebAPI/vocabulary/lookup/identifiers
     * Spring MVC: POST /WebAPI/v2/vocabulary/lookup/identifiers
     */
    @PostMapping(value = "/lookup/identifiers",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeIdentifierLookupDefault(@RequestBody long[] identifiers) {
        Collection<Concept> concepts = vocabularyService.executeIdentifierLookup(identifiers);
        return ok(concepts);
    }

    /**
     * Get concepts from source codes from a specific source
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/lookup/sourcecodes
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/lookup/sourcecodes
     */
    @PostMapping(value = "/{sourceKey}/lookup/sourcecodes",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSourcecodeLookup(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody String[] sourcecodes) {
        Collection<Concept> concepts = vocabularyService.executeSourcecodeLookup(sourceKey, sourcecodes);
        return ok(concepts);
    }

    /**
     * Get concepts from source codes from the default vocabulary source
     *
     * Jersey: POST /WebAPI/vocabulary/lookup/sourcecodes
     * Spring MVC: POST /WebAPI/v2/vocabulary/lookup/sourcecodes
     */
    @PostMapping(value = "/lookup/sourcecodes",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSourcecodeLookupDefault(@RequestBody String[] sourcecodes) {
        Collection<Concept> concepts = vocabularyService.executeSourcecodeLookup(sourcecodes);
        return ok(concepts);
    }

    /**
     * Get concepts mapped to the selected concept identifiers from a specific source
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/lookup/mapped
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/lookup/mapped
     */
    @PostMapping(value = "/{sourceKey}/lookup/mapped",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeMappedLookup(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody long[] identifiers) {
        Collection<Concept> concepts = vocabularyService.executeMappedLookup(sourceKey, identifiers);
        return ok(concepts);
    }

    /**
     * Get concepts mapped to the selected concept identifiers from the default source
     *
     * Jersey: POST /WebAPI/vocabulary/lookup/mapped
     * Spring MVC: POST /WebAPI/v2/vocabulary/lookup/mapped
     */
    @PostMapping(value = "/lookup/mapped",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeMappedLookupDefault(@RequestBody long[] identifiers) {
        Collection<Concept> concepts = vocabularyService.executeMappedLookup(identifiers);
        return ok(concepts);
    }

    /**
     * Search for a concept on the selected source (POST with ConceptSearch)
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/search
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/search
     */
    @PostMapping(value = "/{sourceKey}/search",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSearchPost(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptSearch search) {
        Collection<Concept> concepts = vocabularyService.executeSearch(sourceKey, search);
        return ok(concepts);
    }

    /**
     * Search for a concept on the default vocabulary source (POST with ConceptSearch)
     *
     * Jersey: POST /WebAPI/vocabulary/search
     * Spring MVC: POST /WebAPI/v2/vocabulary/search
     */
    @PostMapping(value = "/search",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSearchPostDefault(@RequestBody ConceptSearch search) {
        Collection<Concept> concepts = vocabularyService.executeSearch(search);
        return ok(concepts);
    }

    /**
     * Search for a concept based on a query using the selected vocabulary source (with path variable)
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/search/{query}
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/search/{query}
     */
    @GetMapping(value = "/{sourceKey}/search/{query}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSearchPathQuery(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("query") String query) {
        Collection<Concept> concepts = vocabularyService.executeSearch(sourceKey, query);
        return ok(concepts);
    }

    /**
     * Search for a concept using query parameters
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/search?query=...&rows=...
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/search?query=...&rows=...
     */
    @GetMapping(value = "/{sourceKey}/search",
                produces = MediaType.APPLICATION_JSON_VALUE,
                params = "query")
    public ResponseEntity<Collection<Concept>> executeSearchQueryParam(
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam("query") String query,
            @RequestParam(value = "rows", defaultValue = VocabularyService.DEFAULT_SEARCH_ROWS) String rows) {
        Collection<Concept> concepts = vocabularyService.executeSearch(sourceKey, query, rows);
        return ok(concepts);
    }

    /**
     * Search for a concept based on a query using the default vocabulary source (path variable)
     *
     * Jersey: GET /WebAPI/vocabulary/search/{query}
     * Spring MVC: GET /WebAPI/v2/vocabulary/search/{query}
     */
    @GetMapping(value = "/search/{query}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> executeSearchDefaultPathQuery(@PathVariable("query") String query) {
        Collection<Concept> concepts = vocabularyService.executeSearch(query);
        return ok(concepts);
    }

    /**
     * Get a concept based on the concept identifier from the specified source
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/concept/{id}
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/concept/{id}
     */
    @GetMapping(value = "/{sourceKey}/concept/{id}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Concept> getConcept(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") long id) {
        Concept concept = vocabularyService.getConcept(sourceKey, id);
        return ok(concept);
    }

    /**
     * Get a concept based on the concept identifier from the default vocabulary source
     *
     * Jersey: GET /WebAPI/vocabulary/concept/{id}
     * Spring MVC: GET /WebAPI/v2/vocabulary/concept/{id}
     */
    @GetMapping(value = "/concept/{id}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Concept> getConceptDefault(@PathVariable("id") long id) {
        Concept concept = vocabularyService.getConcept(id);
        return ok(concept);
    }

    /**
     * Get related concepts for the selected concept identifier from a source
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/concept/{id}/related
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/concept/{id}/related
     */
    @GetMapping(value = "/{sourceKey}/concept/{id}/related",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getRelatedConcepts(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") Long id) {
        Collection<RelatedConcept> concepts = vocabularyService.getRelatedConcepts(sourceKey, id);
        return ok(concepts);
    }

    /**
     * Get related standard mapped concepts
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/related-standard
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/related-standard
     */
    @PostMapping(value = "/{sourceKey}/related-standard",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<MappedRelatedConcept>> getRelatedStandardMappedConcepts(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody List<Long> allConceptIds) {
        Collection<MappedRelatedConcept> concepts = vocabularyService.getRelatedStandardMappedConcepts(sourceKey, allConceptIds);
        return ok(concepts);
    }

    /**
     * Get ancestor and descendant concepts for the selected concept identifier
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/concept/{id}/ancestorAndDescendant
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/concept/{id}/ancestorAndDescendant
     */
    @GetMapping(value = "/{sourceKey}/concept/{id}/ancestorAndDescendant",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getConceptAncestorAndDescendant(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") Long id) {
        Collection<RelatedConcept> concepts = vocabularyService.getConceptAncestorAndDescendant(sourceKey, id);
        return ok(concepts);
    }

    /**
     * Get related concepts for the selected concept identifier (default vocabulary)
     *
     * Jersey: GET /WebAPI/vocabulary/concept/{id}/related
     * Spring MVC: GET /WebAPI/v2/vocabulary/concept/{id}/related
     */
    @GetMapping(value = "/concept/{id}/related",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getRelatedConceptsDefault(@PathVariable("id") Long id) {
        Collection<RelatedConcept> concepts = vocabularyService.getRelatedConcepts(id);
        return ok(concepts);
    }

    /**
     * Get common ancestor concepts
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/commonAncestors
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/commonAncestors
     */
    @PostMapping(value = "/{sourceKey}/commonAncestors",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getCommonAncestors(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody Object[] identifiers) {
        Collection<RelatedConcept> concepts = vocabularyService.getCommonAncestors(sourceKey, identifiers);
        return ok(concepts);
    }

    /**
     * Get common ancestor concepts (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/commonAncestors
     * Spring MVC: POST /WebAPI/v2/vocabulary/commonAncestors
     */
    @PostMapping(value = "/commonAncestors",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getCommonAncestorsDefault(@RequestBody Object[] identifiers) {
        Collection<RelatedConcept> concepts = vocabularyService.getCommonAncestors(identifiers);
        return ok(concepts);
    }

    /**
     * Resolve a concept set expression into a collection of concept identifiers
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/resolveConceptSetExpression
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/resolveConceptSetExpression
     */
    @PostMapping(value = "/{sourceKey}/resolveConceptSetExpression",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Long>> resolveConceptSetExpression(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptSetExpression conceptSetExpression) {
        Collection<Long> identifiers = vocabularyService.resolveConceptSetExpression(sourceKey, conceptSetExpression);
        return ok(identifiers);
    }

    /**
     * Resolve a concept set expression (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/resolveConceptSetExpression
     * Spring MVC: POST /WebAPI/v2/vocabulary/resolveConceptSetExpression
     */
    @PostMapping(value = "/resolveConceptSetExpression",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Long>> resolveConceptSetExpressionDefault(
            @RequestBody ConceptSetExpression conceptSetExpression) {
        Collection<Long> identifiers = vocabularyService.resolveConceptSetExpression(conceptSetExpression);
        return ok(identifiers);
    }

    /**
     * Get included concept counts for concept set expression
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/included-concepts/count
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/included-concepts/count
     */
    @PostMapping(value = "/{sourceKey}/included-concepts/count",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> countIncludedConceptSets(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptSetExpression conceptSetExpression) {
        Integer count = vocabularyService.countIncludedConceptSets(sourceKey, conceptSetExpression);
        return ok(count);
    }

    /**
     * Get included concept counts for concept set expression (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/included-concepts/count
     * Spring MVC: POST /WebAPI/v2/vocabulary/included-concepts/count
     */
    @PostMapping(value = "/included-concepts/count",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> countIncludedConceptSetsDefault(
            @RequestBody ConceptSetExpression conceptSetExpression) {
        Integer count = vocabularyService.countIncludedConcepSets(conceptSetExpression);
        return ok(count);
    }

    /**
     * Get SQL to resolve concept set expression
     *
     * Jersey: POST /WebAPI/vocabulary/conceptSetExpressionSQL
     * Spring MVC: POST /WebAPI/v2/vocabulary/conceptSetExpressionSQL
     */
    @PostMapping(value = "/conceptSetExpressionSQL",
                 produces = MediaType.TEXT_PLAIN_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getConceptSetExpressionSQL(@RequestBody ConceptSetExpression conceptSetExpression) {
        String sql = vocabularyService.getConceptSetExpressionSQL(conceptSetExpression);
        return ok(sql);
    }

    /**
     * Get descendant concepts for the selected concept identifier
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/concept/{id}/descendants
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/concept/{id}/descendants
     */
    @GetMapping(value = "/{sourceKey}/concept/{id}/descendants",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getDescendantConcepts(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") Long id) {
        Collection<RelatedConcept> concepts = vocabularyService.getDescendantConcepts(sourceKey, id);
        return ok(concepts);
    }

    /**
     * Get descendant concepts (default vocabulary)
     *
     * Jersey: GET /WebAPI/vocabulary/concept/{id}/descendants
     * Spring MVC: GET /WebAPI/v2/vocabulary/concept/{id}/descendants
     */
    @GetMapping(value = "/concept/{id}/descendants",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getDescendantConceptsDefault(@PathVariable("id") Long id) {
        Collection<RelatedConcept> concepts = vocabularyService.getDescendantConcepts(id);
        return ok(concepts);
    }

    /**
     * Get domains
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/domains
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/domains
     */
    @GetMapping(value = "/{sourceKey}/domains",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Domain>> getDomains(@PathVariable("sourceKey") String sourceKey) {
        Collection<Domain> domains = vocabularyService.getDomains(sourceKey);
        return ok(domains);
    }

    /**
     * Get domains (default vocabulary)
     *
     * Jersey: GET /WebAPI/vocabulary/domains
     * Spring MVC: GET /WebAPI/v2/vocabulary/domains
     */
    @GetMapping(value = "/domains",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Domain>> getDomainsDefault() {
        Collection<Domain> domains = vocabularyService.getDomains();
        return ok(domains);
    }

    /**
     * Get vocabularies
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/vocabularies
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/vocabularies
     */
    @GetMapping(value = "/{sourceKey}/vocabularies",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Vocabulary>> getVocabularies(@PathVariable("sourceKey") String sourceKey) {
        Collection<Vocabulary> vocabularies = vocabularyService.getVocabularies(sourceKey);
        return ok(vocabularies);
    }

    /**
     * Get vocabularies (default vocabulary)
     *
     * Jersey: GET /WebAPI/vocabulary/vocabularies
     * Spring MVC: GET /WebAPI/v2/vocabulary/vocabularies
     */
    @GetMapping(value = "/vocabularies",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Vocabulary>> getVocabulariesDefault() {
        Collection<Vocabulary> vocabularies = vocabularyService.getVocabularies();
        return ok(vocabularies);
    }

    /**
     * Get vocabulary version info
     *
     * Jersey: GET /WebAPI/vocabulary/{sourceKey}/info
     * Spring MVC: GET /WebAPI/v2/vocabulary/{sourceKey}/info
     */
    @GetMapping(value = "/{sourceKey}/info",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VocabularyInfo> getInfo(@PathVariable("sourceKey") String sourceKey) {
        VocabularyInfo info = vocabularyService.getInfo(sourceKey);
        return ok(info);
    }

    /**
     * Get descendant concepts by source
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/descendantofancestor
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/descendantofancestor
     */
    @PostMapping(value = "/{sourceKey}/descendantofancestor",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> getDescendantOfAncestorConcepts(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody DescendentOfAncestorSearch search) {
        Collection<Concept> concepts = vocabularyService.getDescendantOfAncestorConcepts(sourceKey, search);
        return ok(concepts);
    }

    /**
     * Get descendant concepts (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/descendantofancestor
     * Spring MVC: POST /WebAPI/v2/vocabulary/descendantofancestor
     */
    @PostMapping(value = "/descendantofancestor",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> getDescendantOfAncestorConceptsDefault(
            @RequestBody DescendentOfAncestorSearch search) {
        Collection<Concept> concepts = vocabularyService.getDescendantOfAncestorConcepts(search);
        return ok(concepts);
    }

    /**
     * Get related concepts
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/relatedconcepts
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/relatedconcepts
     */
    @PostMapping(value = "/{sourceKey}/relatedconcepts",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> getRelatedConceptsFiltered(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody RelatedConceptSearch search) {
        Collection<Concept> concepts = vocabularyService.getRelatedConcepts(sourceKey, search);
        return ok(concepts);
    }

    /**
     * Get related concepts (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/relatedconcepts
     * Spring MVC: POST /WebAPI/v2/vocabulary/relatedconcepts
     */
    @PostMapping(value = "/relatedconcepts",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Concept>> getRelatedConceptsFilteredDefault(
            @RequestBody RelatedConceptSearch search) {
        Collection<Concept> concepts = vocabularyService.getRelatedConcepts(search);
        return ok(concepts);
    }

    /**
     * Get descendant concepts for selected concepts
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/conceptlist/descendants
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/conceptlist/descendants
     */
    @PostMapping(value = "/{sourceKey}/conceptlist/descendants",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getDescendantConceptsByList(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody String[] conceptList) {
        Collection<RelatedConcept> concepts = vocabularyService.getDescendantConceptsByList(sourceKey, conceptList);
        return ok(concepts);
    }

    /**
     * Get descendant concepts for selected concepts (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/conceptlist/descendants
     * Spring MVC: POST /WebAPI/v2/vocabulary/conceptlist/descendants
     */
    @PostMapping(value = "/conceptlist/descendants",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RelatedConcept>> getDescendantConceptsByListDefault(
            @RequestBody String[] conceptList) {
        Collection<RelatedConcept> concepts = vocabularyService.getDescendantConceptsByList(conceptList);
        return ok(concepts);
    }

    /**
     * Get recommended concepts for selected concepts
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/lookup/recommended
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/lookup/recommended
     */
    @PostMapping(value = "/{sourceKey}/lookup/recommended",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<RecommendedConcept>> getRecommendedConceptsByList(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody long[] conceptList) {
        Collection<RecommendedConcept> concepts = vocabularyService.getRecommendedConceptsByList(sourceKey, conceptList);
        return ok(concepts);
    }

    /**
     * Compare concept sets
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/compare
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/compare
     */
    @PostMapping(value = "/{sourceKey}/compare",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ConceptSetComparison>> compareConceptSets(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptSetExpression[] conceptSetExpressionList) throws Exception {
        Collection<ConceptSetComparison> comparison = vocabularyService.compareConceptSets(sourceKey, conceptSetExpressionList);
        return ok(comparison);
    }

    /**
     * Compare concept sets (arbitrary/CSV)
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/compare-arbitrary
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/compare-arbitrary
     */
    @PostMapping(value = "/{sourceKey}/compare-arbitrary",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ConceptSetComparison>> compareConceptSetsCsv(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody CompareArbitraryDto dto) throws Exception {
        Collection<ConceptSetComparison> comparison = vocabularyService.compareConceptSetsCsv(sourceKey, dto);
        return ok(comparison);
    }

    /**
     * Compare concept sets (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/compare
     * Spring MVC: POST /WebAPI/v2/vocabulary/compare
     */
    @PostMapping(value = "/compare",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ConceptSetComparison>> compareConceptSetsDefault(
            @RequestBody ConceptSetExpression[] conceptSetExpressionList) throws Exception {
        Collection<ConceptSetComparison> comparison = vocabularyService.compareConceptSets(conceptSetExpressionList);
        return ok(comparison);
    }

    /**
     * Optimize concept set (default vocabulary)
     *
     * Jersey: POST /WebAPI/vocabulary/optimize
     * Spring MVC: POST /WebAPI/v2/vocabulary/optimize
     */
    @PostMapping(value = "/optimize",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptSetOptimizationResult> optimizeConceptSetDefault(
            @RequestBody ConceptSetExpression conceptSetExpression) throws Exception {
        ConceptSetOptimizationResult result = vocabularyService.optimizeConceptSet(conceptSetExpression);
        return ok(result);
    }

    /**
     * Optimize concept set
     *
     * Jersey: POST /WebAPI/vocabulary/{sourceKey}/optimize
     * Spring MVC: POST /WebAPI/v2/vocabulary/{sourceKey}/optimize
     */
    @PostMapping(value = "/{sourceKey}/optimize",
                 produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptSetOptimizationResult> optimizeConceptSet(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ConceptSetExpression conceptSetExpression) throws Exception {
        ConceptSetOptimizationResult result = vocabularyService.optimizeConceptSet(sourceKey, conceptSetExpression);
        return ok(result);
    }
}
