package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** MCP tools for cohort definitions: browse, edit, generate, and read generation results. */
@Component
public class CohortTools implements McpToolset {

    private final CohortDefinitionService cohorts;
    private final McpToolContext context;

    public CohortTools(CohortDefinitionService cohorts, McpToolContext context) {
        this.cohorts = cohorts;
        this.context = context;
    }

    @Tool(description = "List all cohort definitions (id, name, metadata).")
    public McpResult cohortList() {
        return McpCall.guard(cohorts::getCohortDefinitionList);
    }

    @Tool(description = "Get a cohort definition (including its Circe expression JSON) by id.")
    public McpResult cohortGet(
            @ToolParam(description = "Cohort definition id") int id) {
        return McpCall.guard(() -> cohorts.getCohortDefinitionRaw(id));
    }

    @Tool(description = "Create a new cohort definition from a CohortDTO (name, description, expressionType, expression).")
    public McpResult cohortCreate(
            @ToolParam(description = "CohortDTO JSON") CohortDTO dto) {
        return McpCall.guard(() -> cohorts.createCohortDefinition(dto));
    }

    @Tool(description = "Update an existing cohort definition by id.")
    public McpResult cohortUpdate(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "CohortDTO JSON with updated fields") CohortDTO dto) {
        return McpCall.guard(() -> cohorts.saveCohortDefinition(id, dto));
    }

    @Tool(description = "Trigger generation of a cohort against a data source. Generation runs long SQL against "
            + "the source's CDM and can take minutes; this call returns immediately with a job handle. "
            + "Poll job_status with the returned execution id to check completion.")
    public McpResult cohortGenerate(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cohorts.generateCohort(id, context.requireSource(sourceKey), false));
    }

    @Tool(description = "Get generation status/info for a cohort across sources (counts, dates, state).")
    public McpResult cohortGenerationStatus(
            @ToolParam(description = "Cohort definition id") int id) {
        return McpCall.guard(() -> cohorts.getInfo(id));
    }

    @Tool(description = "Get the inclusion-rule / attrition report for a cohort already generated on a source.")
    public McpResult cohortInclusionReport(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cohorts.getInclusionRuleReport(id, context.requireSource(sourceKey), 0));
    }
}
