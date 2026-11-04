package org.ohdsi.webapi.mcp;

import static org.ohdsi.webapi.mcp.ToolDefinition.booleanParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.integerParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.stringParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.mcp.ToolDefinition.Rationale;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * STEP 2 tools: build concept sets one verified concept at a time. Every change carries a
 * clinical rationale, and a concept is only added after its id and name are checked against the
 * vocabulary, so a hallucinated or mistyped concept id is refused instead of saved.
 */
@Component
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class ConceptSetMcpTools implements McpToolProvider {

    private final ConceptSetService conceptSetService;
    private final VocabularyService vocabularyService;
    private final VocabularyMcpTools vocabularyTools;

    public ConceptSetMcpTools(ConceptSetService conceptSetService, VocabularyService vocabularyService,
            VocabularyMcpTools vocabularyTools) {
        this.conceptSetService = conceptSetService;
        this.vocabularyService = vocabularyService;
        this.vocabularyTools = vocabularyTools;
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                tool("create_concept_set")
                        .title("Create concept set")
                        .description("Creates an empty, named concept set owned by the current user. Add concepts "
                                + "with add_concept_to_set.")
                        .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                        .rationale(Rationale.REQUIRED)
                        .requiredParam("name", stringParam("Concept set name; must be unique in WebAPI."))
                        .param("description", stringParam("What the concept set is meant to capture."))
                        .handler(this::create)
                        .build(),
                tool("get_concept_set")
                        .title("Get concept set")
                        .description("Returns a concept set's items with their flags and the concept set expression "
                                + "JSON, ready to embed in a cohort definition's ConceptSets.")
                        .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                        .readOnly()
                        .requiredParam("concept_set_id", integerParam("The concept set id."))
                        .param("source_key", stringParam("Vocabulary source used to fill in concept details. "
                                + "Defaults to the priority vocabulary source."))
                        .handler(this::get)
                        .build(),
                tool("add_concept_to_set")
                        .title("Add concept to concept set")
                        .description("Adds a concept to a concept set, or updates its flags if it is already there. "
                                + "The concept is looked up in the source vocabulary first and the call is refused "
                                + "if the id does not exist or concept_name does not match the vocabulary's name. "
                                + "Non-standard or invalid concepts are accepted but reported as warnings.")
                        .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                        .rationale(Rationale.REQUIRED)
                        .requiredParam("concept_set_id", integerParam("The concept set to change."))
                        .requiredParam("source_key", stringParam("Vocabulary source used to verify the concept."))
                        .requiredParam("concept_id", integerParam("The concept id, as found by search_concepts."))
                        .requiredParam("concept_name", stringParam("The concept's name exactly as the vocabulary has it."))
                        .requiredParam("include_descendants", booleanParam("Include all descendants of the concept."))
                        .param("include_mapped", booleanParam("Include source concepts mapped to it. Default false."))
                        .param("is_excluded", booleanParam("Exclude rather than include the concept. Default false."))
                        .handler(this::addConcept)
                        .build(),
                tool("remove_concept_from_set")
                        .title("Remove concept from concept set")
                        .description("Removes a concept from a concept set.")
                        .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                        .destructive()
                        .rationale(Rationale.REQUIRED)
                        .requiredParam("concept_set_id", integerParam("The concept set to change."))
                        .requiredParam("concept_id", integerParam("The concept id to remove."))
                        .handler(this::removeConcept)
                        .build(),
                tool("count_concept_set_concepts")
                        .title("Count resolved concepts")
                        .description("Resolves the concept set against a source vocabulary (applying descendants, "
                                + "mapped concepts and exclusions) and returns how many concepts it includes.")
                        .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                        .readOnly()
                        .requiredParam("concept_set_id", integerParam("The concept set id."))
                        .requiredParam("source_key", stringParam("Vocabulary source to resolve against."))
                        .handler(this::count)
                        .build());
    }

    private Map<String, Object> create(ToolArguments args) {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName(args.string("name"));
        dto.setDescription(args.optString("description"));
        ConceptSetDTO created = conceptSetService.createConceptSet(dto);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept_set_id", created.getId());
        out.put("name", created.getName());
        return out;
    }

    private Map<String, Object> get(ToolArguments args) {
        int id = args.intValue("concept_set_id");
        ConceptSetDTO dto = conceptSetService.getConceptSet(id);
        ConceptSetExpression expression = args.has("source_key")
                ? conceptSetService.getConceptSetExpressionByIdAndSource(id, vocabularyTools.requireSource(args))
                : conceptSetService.getConceptSetExpressionById(id);
        List<Map<String, Object>> items = new ArrayList<>();
        if (expression.items != null) {
            for (ConceptSetExpression.ConceptSetItem item : expression.items) {
                Map<String, Object> m = ConceptViews.view(item.concept);
                m.put("include_descendants", item.includeDescendants);
                m.put("include_mapped", item.includeMapped);
                m.put("is_excluded", item.isExcluded);
                items.add(m);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept_set_id", dto.getId());
        out.put("name", dto.getName());
        out.put("description", dto.getDescription());
        out.put("items", items);
        out.put("expression", expression);
        return out;
    }

    private Map<String, Object> addConcept(ToolArguments args) {
        int conceptSetId = args.intValue("concept_set_id");
        String sourceKey = vocabularyTools.requireSource(args);
        long conceptId = args.longValue("concept_id");
        String claimedName = args.string("concept_name");
        boolean includeDescendants = args.bool("include_descendants");
        boolean includeMapped = args.optBool("include_mapped", false);
        boolean excluded = args.optBool("is_excluded", false);

        Concept concept = verifiedConcept(sourceKey, conceptId, claimedName);

        List<ConceptSetItem> items = new ArrayList<>();
        conceptSetService.getConceptSetItems(conceptSetId).forEach(items::add);
        ConceptSetItem existing = items.stream().filter(i -> i.getConceptId() == conceptId).findFirst().orElse(null);
        ConceptSetItem item = existing != null ? existing : new ConceptSetItem();
        item.setConceptId(conceptId);
        item.setIncludeDescendants(includeDescendants ? 1 : 0);
        item.setIncludeMapped(includeMapped ? 1 : 0);
        item.setIsExcluded(excluded ? 1 : 0);
        if (existing == null) {
            items.add(item);
        }
        conceptSetService.saveConceptSetItems(conceptSetId, items.toArray(new ConceptSetItem[0]));

        List<String> warnings = new ArrayList<>();
        if (!excluded && !ConceptViews.isStandard(concept)) {
            warnings.add("Concept " + conceptId + " is not a standard concept (standard_concept="
                    + concept.standardConcept + "); cohort criteria match standard concepts, so it will only match "
                    + "via include_mapped or not at all.");
        }
        if (!ConceptViews.isValid(concept)) {
            warnings.add("Concept " + conceptId + " is not valid (invalid_reason=" + concept.invalidReason
                    + "); consider its replacement concept.");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept_set_id", conceptSetId);
        out.put("action", existing == null ? "added" : "updated");
        out.put("concept", ConceptViews.view(concept));
        out.put("include_descendants", includeDescendants);
        out.put("include_mapped", includeMapped);
        out.put("is_excluded", excluded);
        out.put("item_count", items.size());
        out.put("warnings", warnings);
        return out;
    }

    private Map<String, Object> removeConcept(ToolArguments args) {
        int conceptSetId = args.intValue("concept_set_id");
        long conceptId = args.longValue("concept_id");
        List<ConceptSetItem> items = new ArrayList<>();
        conceptSetService.getConceptSetItems(conceptSetId).forEach(items::add);
        boolean removed = items.removeIf(i -> i.getConceptId() == conceptId);
        if (!removed) {
            throw new ToolContractException("Concept " + conceptId + " is not in concept set " + conceptSetId);
        }
        conceptSetService.saveConceptSetItems(conceptSetId, items.toArray(new ConceptSetItem[0]));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept_set_id", conceptSetId);
        out.put("action", "removed");
        out.put("concept_id", conceptId);
        out.put("item_count", items.size());
        return out;
    }

    private Map<String, Object> count(ToolArguments args) {
        int id = args.intValue("concept_set_id");
        String sourceKey = vocabularyTools.requireSource(args);
        ConceptSetExpression expression = conceptSetService.getConceptSetExpressionByIdAndSource(id, sourceKey);
        Integer count = vocabularyService.countIncludedConceptSets(sourceKey, expression);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("concept_set_id", id);
        out.put("source_key", sourceKey);
        out.put("included_concept_count", count);
        return out;
    }

    /**
     * Looks the concept up in the vocabulary and refuses it unless the caller's name for it matches.
     * This is the server-side guard against concept ids that were assumed rather than looked up.
     */
    Concept verifiedConcept(String sourceKey, long conceptId, String claimedName) {
        Concept concept;
        try {
            concept = vocabularyService.getConcept(sourceKey, conceptId);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ToolContractException("Concept " + conceptId + " does not exist in the vocabulary of source '"
                        + sourceKey + "'. Find the concept with search_concepts instead of assuming an id.");
            }
            throw e;
        }
        if (!ConceptViews.sameName(claimedName, concept.conceptName)) {
            throw new ToolContractException("Concept " + conceptId + " is '" + concept.conceptName + "' in source '"
                    + sourceKey + "', not '" + claimedName + "'. Verify the id with search_concepts or "
                    + "get_concept_details before adding it.");
        }
        return concept;
    }
}
