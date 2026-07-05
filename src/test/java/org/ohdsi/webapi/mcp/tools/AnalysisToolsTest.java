package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.feanalysis.FeAnalysisController;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.ircalc.AnalysisInfoDTO;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.IRAnalysisService;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisDTO;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalysisToolsTest {

    private final CcController cc = mock(CcController.class);
    private final IRAnalysisService ir = mock(IRAnalysisService.class);
    private final PathwayController pathway = mock(PathwayController.class);
    private final FeAnalysisController fe = mock(FeAnalysisController.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final AnalysisTools tools = new AnalysisTools(cc, ir, pathway, fe, context);

    // ---- Characterization ----

    @Test
    void characListDelegatesToCcController() {
        Page<CcShortDTO> page = new PageImpl<>(List.of());
        when(cc.list(any(Pageable.class))).thenReturn(page);

        McpResult r = tools.characList(1, 5);

        assertThat(r.ok()).isTrue();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cc).list(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void characGetDelegatesToCcController() {
        CcShortDTO dto = new CcShortDTO();
        when(cc.get(7L)).thenReturn(dto);

        McpResult r = tools.characGet(7L);

        assertThat(r.ok()).isTrue();
        verify(cc).get(7L);
    }

    @Test
    void characCreateDelegatesToCcController() {
        CohortCharacterizationDTO input = new CohortCharacterizationDTO();
        CohortCharacterizationDTO created = new CohortCharacterizationDTO();
        when(cc.create(input)).thenReturn(created);

        McpResult r = tools.characCreate(input);

        assertThat(r.ok()).isTrue();
        verify(cc).create(input);
    }

    @Test
    void characGenerateDelegatesAfterValidatingSourceKey() {
        JobExecutionResource job = new JobExecutionResource();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(cc.generate(1L, "DEMO_CDM")).thenReturn(job);

        McpResult r = tools.characGenerate(1L, "DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(cc).generate(1L, "DEMO_CDM");
    }

    @Test
    void characGenerateValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.characGenerate(1L, "BAD");

        assertThat(r.status()).isEqualTo("invalid_input");
    }

    @Test
    void characResultsDelegatesToCcController() {
        when(cc.getGenerationsResults(5L, 0.01f)).thenReturn(List.<CcResult>of());

        McpResult r = tools.characResults(5L, null);

        assertThat(r.ok()).isTrue();
        verify(cc).getGenerationsResults(5L, 0.01f);
    }

    // ---- Incidence rate ----

    @Test
    void irListReturnsAnalyses() {
        when(ir.getIRAnalysisList()).thenReturn(List.of());

        McpResult r = tools.irList();

        assertThat(r.ok()).isTrue();
        verify(ir).getIRAnalysisList();
    }

    @Test
    void irGetDelegatesToIRAnalysisService() {
        IRAnalysisDTO dto = new IRAnalysisDTO();
        when(ir.getAnalysis(3)).thenReturn(dto);

        McpResult r = tools.irGet(3);

        assertThat(r.ok()).isTrue();
        verify(ir).getAnalysis(3);
    }

    @Test
    void irCreateDelegatesToIRAnalysisService() {
        IRAnalysisDTO input = new IRAnalysisDTO();
        IRAnalysisDTO created = new IRAnalysisDTO();
        when(ir.createAnalysis(input)).thenReturn(created);

        McpResult r = tools.irCreate(input);

        assertThat(r.ok()).isTrue();
        verify(ir).createAnalysis(input);
    }

    @Test
    void irExecuteDelegatesAfterValidatingSourceKey() {
        JobExecutionResource job = new JobExecutionResource();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(ir.performAnalysis(5, "DEMO_CDM")).thenReturn(job);

        McpResult r = tools.irExecute(5, "DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(ir).performAnalysis(5, "DEMO_CDM");
    }

    @Test
    void irExecuteValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));
        McpResult r = tools.irExecute(5, "BAD");
        assertThat(r.status()).isEqualTo("invalid_input");
    }

    @Test
    void irResultsDelegatesAfterValidatingSourceKey() {
        AnalysisReport report = new AnalysisReport();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(ir.getAnalysisReport(5, "DEMO_CDM", 1, 2)).thenReturn(report);

        McpResult r = tools.irResults(5, "DEMO_CDM", 1, 2);

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(ir).getAnalysisReport(5, "DEMO_CDM", 1, 2);
    }

    @Test
    void irStatusDelegatesToIRAnalysisService() {
        when(ir.getAnalysisInfo(5)).thenReturn(List.<AnalysisInfoDTO>of());

        McpResult r = tools.irStatus(5);

        assertThat(r.ok()).isTrue();
        verify(ir).getAnalysisInfo(5);
    }

    // ---- Pathway ----

    @Test
    void pathwayListDelegatesToPathwayController() {
        Page<PathwayAnalysisDTO> page = new PageImpl<>(List.of());
        when(pathway.list(any(Pageable.class))).thenReturn(page);

        McpResult r = tools.pathwayList(1, 5);

        assertThat(r.ok()).isTrue();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(pathway).list(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void pathwayGetDelegatesToPathwayController() {
        PathwayAnalysisDTO dto = new PathwayAnalysisDTO();
        when(pathway.get(9)).thenReturn(dto);

        McpResult r = tools.pathwayGet(9);

        assertThat(r.ok()).isTrue();
        verify(pathway).get(9);
    }

    @Test
    void pathwayCreateDelegatesToPathwayController() {
        PathwayAnalysisDTO input = new PathwayAnalysisDTO();
        PathwayAnalysisDTO created = new PathwayAnalysisDTO();
        when(pathway.create(input)).thenReturn(created);

        McpResult r = tools.pathwayCreate(input);

        assertThat(r.ok()).isTrue();
        verify(pathway).create(input);
    }

    @Test
    void pathwayGenerateDelegatesAfterValidatingSourceKey() {
        JobExecutionResource job = new JobExecutionResource();
        when(context.requireSource("DEMO_CDM")).thenReturn("DEMO_CDM");
        when(pathway.generatePathways(4, "DEMO_CDM")).thenReturn(job);

        McpResult r = tools.pathwayGenerate(4, "DEMO_CDM");

        assertThat(r.ok()).isTrue();
        verify(context).requireSource("DEMO_CDM");
        verify(pathway).generatePathways(4, "DEMO_CDM");
    }

    @Test
    void pathwayGenerateValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));

        McpResult r = tools.pathwayGenerate(4, "BAD");

        assertThat(r.status()).isEqualTo("invalid_input");
    }

    @Test
    void pathwayResultsDelegatesToPathwayController() {
        PathwayPopulationResultsDTO results = new PathwayPopulationResultsDTO(List.of(), List.of());
        when(pathway.getGenerationResults(11L)).thenReturn(results);

        McpResult r = tools.pathwayResults(11L);

        assertThat(r.ok()).isTrue();
        verify(pathway).getGenerationResults(11L);
    }

    // ---- Feature analysis ----

    @Test
    void feanalysisListDelegatesToFeAnalysisController() {
        Page<FeAnalysisShortDTO> page = new PageImpl<>(List.of());
        when(fe.list(any(Pageable.class))).thenReturn(page);

        McpResult r = tools.feanalysisList(1, 5);

        assertThat(r.ok()).isTrue();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(fe).list(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void feanalysisGetDelegatesToFeAnalysisController() {
        FeAnalysisDTO dto = new FeAnalysisDTO();
        when(fe.getFeAnalysis(6)).thenReturn(dto);

        McpResult r = tools.feanalysisGet(6);

        assertThat(r.ok()).isTrue();
        verify(fe).getFeAnalysis(6);
    }
}
