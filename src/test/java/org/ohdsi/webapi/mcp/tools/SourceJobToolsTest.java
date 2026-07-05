package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SourceJobToolsTest {

    private final SourceService sourceService = mock(SourceService.class);
    private final org.ohdsi.webapi.service.CDMResultsService cdm = mock(org.ohdsi.webapi.service.CDMResultsService.class);
    private final org.ohdsi.webapi.service.JobService jobs = mock(org.ohdsi.webapi.service.JobService.class);
    private final org.ohdsi.webapi.job.NotificationServiceImpl notifications = mock(org.ohdsi.webapi.job.NotificationServiceImpl.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final SourceJobTools tools = new SourceJobTools(sourceService, cdm, jobs, notifications, context);

    @Test
    void sourceListReturnsSources() {
        SourceInfo info = new SourceInfo(new Source());
        when(sourceService.getSourcesEndpoint())
                .thenReturn(ResponseEntity.ok(List.of(info)));
        McpResult r = tools.sourceList();
        assertThat(r.ok()).isTrue();
    }

    @Test
    void cdmResultsDashboardResolvesSourceKeyThenCallsService() {
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");

        McpResult r = tools.cdmResultsDashboard("DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(cdm).getDashboard("DEMO_CDM");
    }

    @Test
    void cdmResultsDomainCountsResolvesSourceKeyThenCallsService() {
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");

        McpResult r = tools.cdmResultsDomainCounts("DEMO_CDM", "condition");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(cdm).getTreemap("DEMO_CDM", "condition");
    }

    @Test
    void jobStatusReturnsOkEnvelope() {
        JobExecutionResource execution = new JobExecutionResource();
        when(jobs.findJobExecutionById(42L)).thenReturn(execution);

        McpResult r = tools.jobStatus(42L);

        assertThat(r.ok()).isTrue();
        verify(jobs).findJobExecutionById(42L);
    }

    @Test
    void jobListRecentReturnsOkEnvelope() throws Exception {
        McpResult r = tools.jobListRecent();

        assertThat(r.ok()).isTrue();
        verify(jobs).list(null, 0, 20, false);
    }

    @Test
    void unknownSourceKeyBecomesInvalidInput() {
        when(context.requireSource("NOPE"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'NOPE'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.cdmResultsDashboard("NOPE");

        assertThat(r.ok()).isFalse();
        assertThat(r.status()).isEqualTo("invalid_input");
    }
}
