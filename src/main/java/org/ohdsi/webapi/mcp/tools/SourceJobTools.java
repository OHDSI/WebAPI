package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.job.NotificationServiceImpl;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** MCP tools for data sources, CDM result summaries, and job status. Read-only. */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class SourceJobTools implements McpToolset {

    private final SourceService sourceService;
    private final CDMResultsService cdmResults;
    private final JobService jobService;
    // Reserved for a future notifications tool; not yet exposed via @Tool.
    private final NotificationServiceImpl notifications;
    private final McpToolContext context;

    public SourceJobTools(SourceService sourceService, CDMResultsService cdmResults,
                          JobService jobService, NotificationServiceImpl notifications,
                          McpToolContext context) {
        this.sourceService = sourceService;
        this.cdmResults = cdmResults;
        this.jobService = jobService;
        this.notifications = notifications;
        this.context = context;
    }

    @Tool(description = "List all configured CDM data sources with their keys and daimons. "
            + "Call this first to discover valid sourceKey values for other tools.")
    public McpResult sourceList() {
        return McpCall.guard(() -> sourceService.getSourcesEndpoint().getBody());
    }

    @Tool(description = "Get the record-count / data-density dashboard for a source.")
    public McpResult cdmResultsDashboard(
            @ToolParam(description = "Source key (see sourceList)") String sourceKey) {
        return McpCall.guard(() -> cdmResults.getDashboard(context.requireSource(sourceKey)));
    }

    @Tool(description = "Get a per-concept treemap breakdown (patient/record counts by concept) within a "
            + "given OMOP domain for a source, e.g. top concepts within Condition, Drug, Procedure.")
    public McpResult cdmResultsDomainCounts(
            @ToolParam(description = "Source key (see sourceList)") String sourceKey,
            @ToolParam(description = "OMOP domain to break down by concept, e.g. condition, drug") String domain) {
        return McpCall.guard(() -> cdmResults.getTreemap(context.requireSource(sourceKey), domain));
    }

    @Tool(description = "Get the status of a job execution by executionId. Use to poll generation/analysis jobs.")
    public McpResult jobStatus(
            @ToolParam(description = "The execution id returned by a *_generate/*_execute tool") long executionId) {
        return McpCall.guard(() -> jobService.findJobExecutionById(executionId));
    }

    @Tool(description = "List recent job executions (most recent page).")
    public McpResult jobListRecent() {
        return McpCall.guard(() -> jobService.list(null, 0, 20, false));
    }
}
