package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.feanalysis.FeAnalysisController;
import org.ohdsi.webapi.ircalc.IRAnalysisService;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisDTO;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/** MCP tools for the four analysis types: characterization, incidence rate, pathway, feature. */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class AnalysisTools implements McpToolset {

    private final CcController cc;
    private final IRAnalysisService ir;
    private final PathwayController pathway;
    private final FeAnalysisController fe;
    private final McpToolContext context;

    public AnalysisTools(CcController cc, IRAnalysisService ir, PathwayController pathway,
                          FeAnalysisController fe, McpToolContext context) {
        this.cc = cc;
        this.ir = ir;
        this.pathway = pathway;
        this.fe = fe;
        this.context = context;
    }

    private static PageRequest pageRequest(Integer page, Integer size) {
        return PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
    }

    // ---- Cohort characterization ----

    @Tool(description = "List cohort characterizations (paged).")
    public McpResult characList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> cc.list(pageRequest(page, size)));
    }

    @Tool(description = "Get a cohort characterization's metadata by id.")
    public McpResult characGet(@ToolParam(description = "Characterization id") Long id) {
        return McpCall.guard(() -> cc.get(id));
    }

    @Tool(description = "Create a new cohort characterization from a CohortCharacterizationDTO "
            + "(name, cohorts, featureAnalyses, etc). Returns the created characterization with its new id.")
    public McpResult characCreate(
            @ToolParam(description = "CohortCharacterizationDTO JSON") CohortCharacterizationDTO dto) {
        return McpCall.guard(() -> cc.create(dto));
    }

    @Tool(description = "Trigger generation of a cohort characterization against a data source. Generation runs "
            + "long SQL against the source's CDM and can take minutes; this call returns immediately with a job "
            + "handle. Poll job_status with the returned execution id to check completion.")
    public McpResult characGenerate(
            @ToolParam(description = "Characterization id") Long id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cc.generate(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get the results of a characterization generation by generationId, filtered by a minimum "
            + "prevalence threshold (default 0.01 = 1%).")
    public McpResult characResults(
            @ToolParam(description = "Generation id") Long generationId,
            @ToolParam(required = false, description = "Minimum prevalence threshold (default 0.01)") Float thresholdLevel) {
        return McpCall.guard(() -> cc.getGenerationsResults(generationId, thresholdLevel == null ? 0.01f : thresholdLevel));
    }

    // ---- Incidence rate ----

    @Tool(description = "List incidence-rate analyses.")
    public McpResult irList() {
        return McpCall.guard(ir::getIRAnalysisList);
    }

    @Tool(description = "Get an incidence-rate analysis by id.")
    public McpResult irGet(@ToolParam(description = "IR analysis id") int id) {
        return McpCall.guard(() -> ir.getAnalysis(id));
    }

    @Tool(description = "Create a new incidence-rate analysis from an IRAnalysisDTO (name, description, expression). "
            + "Returns the created analysis with its new id.")
    public McpResult irCreate(
            @ToolParam(description = "IRAnalysisDTO JSON") IRAnalysisDTO dto) {
        return McpCall.guard(() -> ir.createAnalysis(dto));
    }

    @Tool(description = "Trigger execution of an incidence-rate analysis against a data source. Execution runs "
            + "long SQL against the source's CDM and can take minutes; this call returns immediately with a job "
            + "handle. Poll job_status with the returned execution id to check completion.")
    public McpResult irExecute(
            @ToolParam(description = "IR analysis id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> ir.performAnalysis(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get the incidence-rate report for an analysis on a source and a target/outcome cohort pair.")
    public McpResult irResults(
            @ToolParam(description = "IR analysis id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "Target cohort id") int targetId,
            @ToolParam(description = "Outcome cohort id") int outcomeId) {
        return McpCall.guard(() -> ir.getAnalysisReport(id, context.requireSource(sourceKey), targetId, outcomeId));
    }

    @Tool(description = "Get execution status/info for an incidence-rate analysis across all sources it has run on.")
    public McpResult irStatus(@ToolParam(description = "IR analysis id") int id) {
        return McpCall.guard(() -> ir.getAnalysisInfo(id));
    }

    // ---- Pathway ----

    @Tool(description = "List pathway analyses (paged).")
    public McpResult pathwayList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> pathway.list(pageRequest(page, size)));
    }

    @Tool(description = "Get a pathway analysis by id.")
    public McpResult pathwayGet(@ToolParam(description = "Pathway analysis id") Integer id) {
        return McpCall.guard(() -> pathway.get(id));
    }

    @Tool(description = "Create a new pathway analysis from a PathwayAnalysisDTO (target/event cohorts, collapse "
            + "and repeat-event settings). Returns the created analysis with its new id.")
    public McpResult pathwayCreate(
            @ToolParam(description = "PathwayAnalysisDTO JSON") PathwayAnalysisDTO dto) {
        return McpCall.guard(() -> pathway.create(dto));
    }

    @Tool(description = "Trigger generation of a pathway analysis against a data source. Generation runs long SQL "
            + "against the source's CDM and can take minutes; this call returns immediately with a job handle. "
            + "Poll job_status with the returned execution id to check completion.")
    public McpResult pathwayGenerate(
            @ToolParam(description = "Pathway analysis id") Integer id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> pathway.generatePathways(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get pathway-analysis results by generationId.")
    public McpResult pathwayResults(@ToolParam(description = "Generation id") Long generationId) {
        return McpCall.guard(() -> pathway.getGenerationResults(generationId));
    }

    // ---- Feature analysis ----

    @Tool(description = "List feature analyses (paged).")
    public McpResult feanalysisList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> fe.list(pageRequest(page, size)));
    }

    @Tool(description = "Get a feature analysis by id.")
    public McpResult feanalysisGet(@ToolParam(description = "Feature analysis id") Integer id) {
        return McpCall.guard(() -> fe.getFeAnalysis(id));
    }
}
