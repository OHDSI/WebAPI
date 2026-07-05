package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** MCP tools for browsing, creating, editing, and resolving concept sets. */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class ConceptSetTools implements McpToolset {

    private final ConceptSetService conceptSets;
    private final VocabularyService vocab;
    private final McpToolContext context;

    public ConceptSetTools(ConceptSetService conceptSets, VocabularyService vocab, McpToolContext context) {
        this.conceptSets = conceptSets;
        this.vocab = vocab;
        this.context = context;
    }

    @Tool(description = "List all concept sets (id and name).")
    public McpResult conceptsetList() {
        return McpCall.guard(conceptSets::getConceptSets);
    }

    @Tool(description = "Get a concept set's metadata by id.")
    public McpResult conceptsetGet(
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> conceptSets.getConceptSet(id));
    }

    @Tool(description = "Get the full expression (concept items with include-descendants / mapped flags) of a concept set.")
    public McpResult conceptsetExpression(
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> conceptSets.getConceptSetExpressionById(id));
    }

    @Tool(description = "Create a new concept set from a ConceptSetDTO (name + description). "
            + "Returns the created concept set with its new id.")
    public McpResult conceptsetCreate(
            @ToolParam(description = "ConceptSetDTO JSON: name and description") ConceptSetDTO dto) {
        return McpCall.guard(() -> conceptSets.createConceptSet(dto));
    }

    @Tool(description = "Update an existing concept set's name/description by id.")
    public McpResult conceptsetUpdate(
            @ToolParam(description = "Concept set id") int id,
            @ToolParam(description = "ConceptSetDTO JSON with updated fields") ConceptSetDTO dto) {
        return McpCall.guard(() -> conceptSets.updateConceptSet(id, dto));
    }

    @Tool(description = "Resolve a concept set's expression to the list of included standard concept ids "
            + "against a data source. Use source_list to find valid sourceKey values.")
    public McpResult conceptsetResolve(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> {
            String key = context.requireSource(sourceKey);
            ConceptSetExpression expression = conceptSets.getConceptSetExpressionById(id);
            return vocab.resolveConceptSetExpression(key, expression);
        });
    }
}
