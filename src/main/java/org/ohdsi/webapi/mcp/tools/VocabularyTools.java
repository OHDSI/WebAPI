package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** MCP tools for browsing the OMOP vocabulary. Read-only. */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class VocabularyTools implements McpToolset {

    private final VocabularyService vocab;
    private final McpToolContext context;

    public VocabularyTools(VocabularyService vocab, McpToolContext context) {
        this.vocab = vocab;
        this.context = context;
    }

    @Tool(description = "Search the OMOP vocabulary for concepts by term. Optional filters: "
            + "domainId (e.g. Condition, Drug), vocabularyId (e.g. SNOMED, RxNorm), "
            + "standardConcept ('S' for standard only). Returns matching concepts.")
    public McpResult vocabSearchConcepts(
            @ToolParam(description = "Free-text search term") String query,
            @ToolParam(required = false, description = "Domain id filter, e.g. Condition") String domainId,
            @ToolParam(required = false, description = "Vocabulary id filter, e.g. SNOMED") String vocabularyId,
            @ToolParam(required = false, description = "'S' to restrict to standard concepts") String standardConcept) {
        return McpCall.guard(() -> {
            ConceptSearch search = new ConceptSearch();
            search.query = query;
            if (domainId != null) search.domainId = new String[]{domainId};
            if (vocabularyId != null) search.vocabularyId = new String[]{vocabularyId};
            search.standardConcept = standardConcept;
            return vocab.executeSearch(search);
        });
    }

    @Tool(description = "Get full details for a single concept by its conceptId.")
    public McpResult vocabGetConcept(
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() -> vocab.getConcept(conceptId));
    }

    @Tool(description = "List concepts directly related to the given conceptId (relationships).")
    public McpResult vocabRelatedConcepts(
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() -> vocab.getRelatedConcepts(conceptId));
    }

    @Tool(description = "List ancestor and descendant concepts in the hierarchy for a conceptId, "
            + "resolved against a data source. Use source_list to find valid sourceKey values.")
    public McpResult vocabConceptAncestors(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() ->
                vocab.getConceptAncestorAndDescendant(context.requireSource(sourceKey), conceptId));
    }

    @Tool(description = "List descendant concepts in the hierarchy for a conceptId, "
            + "resolved against a data source. Use source_list to find valid sourceKey values.")
    public McpResult vocabConceptDescendants(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() ->
                vocab.getDescendantConcepts(context.requireSource(sourceKey), conceptId));
    }

    @Tool(description = "List the OMOP domains available for filtering concept searches.")
    public McpResult vocabDomains(
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> vocab.getDomains(context.requireSource(sourceKey)));
    }

    @Tool(description = "List the OMOP vocabularies available for filtering concept searches.")
    public McpResult vocabVocabularies(
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> vocab.getVocabularies(context.requireSource(sourceKey)));
    }
}
