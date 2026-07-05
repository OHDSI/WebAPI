package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortGenerationInfoDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CohortToolsTest {

    private final CohortDefinitionService cohorts = mock(CohortDefinitionService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final CohortTools tools = new CohortTools(cohorts, context);

    @Test
    void listReturnsCohorts() {
        when(cohorts.getCohortDefinitionList()).thenReturn(List.of());

        McpResult r = tools.cohortList();

        assertThat(r.ok()).isTrue();
        verify(cohorts).getCohortDefinitionList();
    }

    @Test
    void getReturnsCohortDefinition() {
        CohortRawDTO dto = new CohortRawDTO();
        dto.setId(7);
        when(cohorts.getCohortDefinitionRaw(7)).thenReturn(dto);

        McpResult r = tools.cohortGet(7);

        assertThat(r.ok()).isTrue();
        verify(cohorts).getCohortDefinitionRaw(7);
    }

    @Test
    void createDelegatesToCohortDefinitionService() {
        CohortDTO input = new CohortDTO();
        input.setName("New Cohort");
        CohortDTO created = new CohortDTO();
        created.setId(9);
        when(cohorts.createCohortDefinition(input)).thenReturn(created);

        McpResult r = tools.cohortCreate(input);

        assertThat(r.ok()).isTrue();
        verify(cohorts).createCohortDefinition(input);
    }

    @Test
    void updateDelegatesToCohortDefinitionService() {
        CohortDTO input = new CohortDTO();
        input.setName("Updated Cohort");
        CohortDTO updated = new CohortDTO();
        updated.setId(7);
        when(cohorts.saveCohortDefinition(7, input)).thenReturn(updated);

        McpResult r = tools.cohortUpdate(7, input);

        assertThat(r.ok()).isTrue();
        verify(cohorts).saveCohortDefinition(7, input);
    }

    @Test
    void generateDelegatesAfterValidatingSourceKey() {
        JobExecutionResource job = new JobExecutionResource();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(cohorts.generateCohort(1, "DEMO_CDM", false)).thenReturn(job);

        McpResult r = tools.cohortGenerate(1, "DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(cohorts).generateCohort(1, "DEMO_CDM", false);
    }

    @Test
    void generateValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.cohortGenerate(1, "BAD");

        assertThat(r.status()).isEqualTo("invalid_input");
        assertThat(r.message()).contains("DEMO_CDM");
    }

    @Test
    void generationStatusReturnsInfo() {
        when(cohorts.getInfo(1)).thenReturn(List.<CohortGenerationInfoDTO>of());

        McpResult r = tools.cohortGenerationStatus(1);

        assertThat(r.ok()).isTrue();
        verify(cohorts).getInfo(1);
    }

    @Test
    void inclusionReportDelegatesAfterValidatingSourceKey() {
        InclusionRuleReport report = new InclusionRuleReport();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(cohorts.getInclusionRuleReport(1, "DEMO_CDM", 0)).thenReturn(report);

        McpResult r = tools.cohortInclusionReport(1, "DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(cohorts).getInclusionRuleReport(1, "DEMO_CDM", 0);
    }

    @Test
    void inclusionReportValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.cohortInclusionReport(1, "BAD");

        assertThat(r.status()).isEqualTo("invalid_input");
    }
}
