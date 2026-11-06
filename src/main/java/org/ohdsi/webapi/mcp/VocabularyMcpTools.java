package org.ohdsi.webapi.mcp;

import static org.ohdsi.webapi.mcp.ToolDefinition.booleanParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.integerParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.stringArrayParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.stringParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** STEP 1 tools: find sources and explore the standardized vocabulary. All read-only. */
@Component
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class VocabularyMcpTools implements McpToolProvider {

    static final int DEFAULT_LIMIT = 50;
    static final int MAX_LIMIT = 500;

    private final VocabularyService vocabularyService;
    private final SourceService sourceService;

    public VocabularyMcpTools(VocabularyService vocabularyService, SourceService sourceService) {
        this.vocabularyService = vocabularyService;
        this.sourceService = sourceService;
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                tool("list_sources")
                        .title("List CDM sources")
                        .description("Lists the CDM sources the current user can read. Use a source_key from here in "
                                + "every vocabulary, validation and generation tool.")
                        .step(ReasoningStep.STEP_1_CONCEPT_EXPLORATION)
                        .readOnly()
                        .handler(args -> listSources())
                        .build(),
                tool("search_concepts")
                        .title("Search concepts")
                        .description("Searches the source's standardized vocabulary by name or code. Returns at most "
                                + "'limit' concepts and the total number of matches. Every concept used in a concept set "
                                + "or cohort must first be found here or with get_concept_details.")
                        .step(ReasoningStep.STEP_1_CONCEPT_EXPLORATION)
                        .readOnly()
                        .requiredParam("source_key", stringParam("Source whose vocabulary to search (see list_sources)."))
                        .requiredParam("query", stringParam("Text or code to search for."))
                        .param("domain_id", stringArrayParam("Restrict to these domains, e.g. [\"Condition\"]."))
                        .param("vocabulary_id", stringArrayParam("Restrict to these vocabularies, e.g. [\"SNOMED\"]."))
                        .param("concept_class_id", stringArrayParam("Restrict to these concept classes."))
                        .param("standard_concept", stringParam("'S' standard only, 'C' classification only, 'N' non-standard only."))
                        .param("include_invalid", booleanParam("Include deprecated/updated concepts. Default false."))
                        .param("limit", integerParam("Maximum concepts to return (default " + DEFAULT_LIMIT + ", max " + MAX_LIMIT + ")."))
                        .handler(this::searchConcepts)
                        .build(),
                tool("get_concept_details")
                        .title("Get concept details")
                        .description("Returns one concept by id from the source's vocabulary, or an error if the id "
                                + "does not exist there. Use it to verify any concept id before using it.")
                        .step(ReasoningStep.STEP_1_CONCEPT_EXPLORATION)
                        .readOnly()
                        .requiredParam("source_key", stringParam("Source whose vocabulary to use."))
                        .requiredParam("concept_id", integerParam("The concept id to look up."))
                        .handler(args -> ConceptViews.view(
                                vocabularyService.getConcept(requireSource(args), args.longValue("concept_id"))))
                        .build(),
                tool("get_concept_hierarchy")
                        .title("Get concept hierarchy")
                        .description("Returns the ancestors and descendants of a concept (from CONCEPT_ANCESTOR) with "
                                + "their levels of separation. Use it to decide whether include_descendants is "
                                + "clinically appropriate.")
                        .step(ReasoningStep.STEP_1_CONCEPT_EXPLORATION)
                        .readOnly()
                        .requiredParam("source_key", stringParam("Source whose vocabulary to use."))
                        .requiredParam("concept_id", integerParam("The concept whose hierarchy to return."))
                        .param("max_levels", integerParam("Only return concepts at most this many levels away."))
                        .param("limit", integerParam("Maximum ancestors and maximum descendants to return (default "
                                + DEFAULT_LIMIT + ", max " + MAX_LIMIT + ")."))
                        .handler(this::hierarchy)
                        .build());
    }

    private List<Map<String, Object>> listSources() {
        Collection<SourceInfo> sources = sourceService.getSourcesEndpoint().getBody();
        List<Map<String, Object>> out = new ArrayList<>();
        if (sources != null) {
            for (SourceInfo s : sources) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("source_key", s.sourceKey);
                m.put("source_name", s.sourceName);
                m.put("source_dialect", s.sourceDialect);
                m.put("daimons", s.daimons == null ? List.of()
                        : s.daimons.stream().map(d -> String.valueOf(d.getDaimonType())).distinct().toList());
                out.add(m);
            }
        }
        return out;
    }

    private Map<String, Object> searchConcepts(ToolArguments args) {
        String sourceKey = requireSource(args);
        ConceptSearch search = new ConceptSearch();
        search.query = args.string("query");
        search.domainId = args.optStringArray("domain_id");
        search.vocabularyId = args.optStringArray("vocabulary_id");
        search.conceptClassId = args.optStringArray("concept_class_id");
        String standard = args.optString("standard_concept");
        if (standard != null && !standard.isBlank()) {
            if (!List.of("S", "C", "N").contains(standard)) {
                throw new ToolContractException("'standard_concept' must be 'S', 'C' or 'N'");
            }
            search.standardConcept = standard;
        }
        if (!args.optBool("include_invalid", false)) {
            search.invalidReason = "V";
        }
        int limit = limit(args);

        Collection<Concept> found = vocabularyService.executeSearch(sourceKey, search);
        List<Map<String, Object>> concepts = new ArrayList<>();
        for (Concept c : found) {
            if (concepts.size() >= limit) {
                break;
            }
            concepts.add(ConceptViews.view(c));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source_key", sourceKey);
        out.put("total_matches", found.size());
        out.put("returned", concepts.size());
        out.put("concepts", concepts);
        return out;
    }

    private Map<String, Object> hierarchy(ToolArguments args) {
        String sourceKey = requireSource(args);
        long conceptId = args.longValue("concept_id");
        Integer maxLevels = args.has("max_levels") ? args.intValue("max_levels") : null;
        int limit = limit(args);

        Concept root = vocabularyService.getConcept(sourceKey, conceptId);
        Collection<RelatedConcept> related = vocabularyService.getConceptAncestorAndDescendant(sourceKey, conceptId);

        List<Map<String, Object>> ancestors = new ArrayList<>();
        List<Map<String, Object>> descendants = new ArrayList<>();
        int ancestorTotal = 0;
        int descendantTotal = 0;
        List<RelatedConcept> sorted = new ArrayList<>(related);
        sorted.sort(java.util.Comparator.comparingInt(VocabularyMcpTools::minDistance));
        for (RelatedConcept rc : sorted) {
            for (ConceptRelationship rel : rc.relationships) {
                if (maxLevels != null && rel.relationshipDistance > maxLevels) {
                    continue;
                }
                boolean isAncestor = "Has ancestor of".equals(rel.relationshipName);
                List<Map<String, Object>> bucket = isAncestor ? ancestors : descendants;
                if (isAncestor) {
                    ancestorTotal++;
                } else {
                    descendantTotal++;
                }
                if (bucket.size() < limit) {
                    Map<String, Object> m = ConceptViews.view(rc);
                    m.put("levels_of_separation", rel.relationshipDistance);
                    bucket.add(m);
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept", ConceptViews.view(root));
        out.put("ancestor_count", ancestorTotal);
        out.put("descendant_count", descendantTotal);
        out.put("ancestors", ancestors);
        out.put("descendants", descendants);
        return out;
    }

    private static int minDistance(RelatedConcept rc) {
        return rc.relationships.stream().mapToInt(r -> r.relationshipDistance).min().orElse(Integer.MAX_VALUE);
    }

    /** Returns the 'source_key' argument after checking that such a source exists. */
    String requireSource(ToolArguments args) {
        String sourceKey = args.string("source_key");
        if (sourceService.findBySourceKey(sourceKey) == null) {
            throw new ToolContractException("Unknown source_key '" + sourceKey + "'; call list_sources.");
        }
        return sourceKey;
    }

    static int limit(ToolArguments args) {
        int limit = args.optInt("limit", DEFAULT_LIMIT);
        if (limit < 1) {
            throw new ToolContractException("'limit' must be at least 1");
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
