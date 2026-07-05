package org.ohdsi.webapi.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ohdsi.webapi.test.WebApiIT;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * Authoritative snapshot of the MCP tool-registration surface.
 *
 * <p>Boots the full WebAPI context (embedded Postgres via {@link WebApiIT}) with the MCP
 * {@code ToolCallbackProvider} enabled ({@code mcp.server.enabled=true}) and asserts the exact
 * set of registered tool names. Any accidental addition or removal of a tool will fail this
 * test, forcing the list below to be updated deliberately rather than drifting silently.
 */
@TestPropertySource(properties = {
    "mcp.server.enabled=true",
    "spring.ai.mcp.server.enabled=true"
})
public class McpToolRegistrationIT extends WebApiIT {

    @Autowired
    private ApplicationContext ctx;

    private ToolCallbackProvider provider() {
        // McpServerConfig registers exactly this bean when mcp.server.enabled=true.
        return ctx.getBean("webApiMcpTools", ToolCallbackProvider.class);
    }

    @Test
    public void allExpectedToolsAreRegistered() {
        List<String> names = Arrays.stream(provider().getToolCallbacks())
            .map(t -> t.getToolDefinition().name())
            .toList();

        assertThat(names).containsExactlyInAnyOrder(
            // vocabulary
            "vocabSearchConcepts", "vocabGetConcept", "vocabRelatedConcepts",
            "vocabConceptAncestors", "vocabConceptDescendants", "vocabDomains", "vocabVocabularies",
            // source/jobs/results
            "sourceList", "cdmResultsDashboard", "cdmResultsDomainCounts", "jobStatus", "jobListRecent",
            // concept sets
            "conceptsetList", "conceptsetGet", "conceptsetExpression",
            "conceptsetCreate", "conceptsetUpdate", "conceptsetResolve",
            // cohorts
            "cohortList", "cohortGet", "cohortCreate", "cohortUpdate",
            "cohortGenerate", "cohortGenerationStatus", "cohortInclusionReport",
            // analyses: characterization
            "characList", "characGet", "characCreate", "characGenerate", "characResults",
            // analyses: incidence rate
            "irList", "irGet", "irCreate", "irExecute", "irResults", "irStatus",
            // analyses: pathway
            "pathwayList", "pathwayGet", "pathwayCreate", "pathwayGenerate", "pathwayResults",
            // analyses: feature analysis
            "feanalysisList", "feanalysisGet");
    }
}
