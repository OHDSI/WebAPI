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
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Component;

/** MCP tools for data sources, CDM result summaries, and job status. Read-only. */
@Component
public class SourceJobTools implements McpToolset {

    private final SourceService sourceService;
    private final CDMResultsService cdmResults;
    private final JobService jobService;
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

    @Tool(description = "Get per-domain record counts (treemap) for a source, e.g. Condition, Drug, Procedure.")
    public McpResult cdmResultsDomainCounts(
            @ToolParam(description = "Source key (see sourceList)") String sourceKey,
            @ToolParam(description = "OMOP domain, e.g. condition, drug") String domain) {
        return McpCall.guard(() -> cdmResults.getTreemap(context.requireSource(sourceKey), domain));
    }

    @Tool(description = "Get the status of a job by jobId. Use to poll generation/analysis jobs.")
    public McpResult jobStatus(
            @ToolParam(description = "Job id returned by a *_generate/*_execute tool") long jobId) {
        return McpCall.guard(() -> jobService.findJob(jobId));
    }

    @Tool(description = "List recent job executions (most recent page).")
    public McpResult jobListRecent() {
        return McpCall.guard(() -> {
            try {
                return jobService.list(null, 0, 20, false);
            } catch (NoSuchJobException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
