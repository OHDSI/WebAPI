package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jersey.repackaged.com.google.common.base.Joiner;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysis;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTasklet;
import org.ohdsi.webapi.cohortanalysis.CohortSummary;
import org.ohdsi.webapi.cohortresults.CohortDashboard;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.model.results.Analysis;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * Services related to running Heracles analyses
 */
@Path("/cohortanalysis/")
@Component
public class CohortAnalysisService extends AbstractDaoService {
    
    @Autowired
    private JobTemplate jobTemplate;
    
    @Autowired
    private CohortResultsService resultsService;
    
    @Autowired
    private CohortDefinitionService definitionService;
    
    @Autowired
    private StepBuilderFactory stepBuilders;
    
    private final RowMapper<Analysis> analysisMapper = new RowMapper<Analysis>() {
        
        @Override
        public Analysis mapRow(ResultSet rs, int rowNum) throws SQLException {
            Analysis analysis = new Analysis();
            mapAnalysis(analysis, rs, rowNum);
            return analysis;
        }
        
    };
    
    private final RowMapper<CohortAnalysis> cohortAnalysisMapper = new RowMapper<CohortAnalysis>() {
        
        @Override
        public CohortAnalysis mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final CohortAnalysis cohortAnalysis = new CohortAnalysis();
            mapAnalysis(cohortAnalysis, rs, rowNum);
            cohortAnalysis.setAnalysisComplete(rs.getInt(CohortAnalysis.ANALYSIS_COMPLETE) == 1);
            cohortAnalysis.setCohortDefinitionId(rs.getInt(CohortAnalysis.COHORT_DEFINITION_ID));
            cohortAnalysis.setLastUpdateTime(rs.getTimestamp(CohortAnalysis.LAST_UPDATE_TIME));
            return cohortAnalysis;
        }
    };
    
    private void mapAnalysis(final Analysis analysis, final ResultSet rs, final int rowNum) throws SQLException {
        analysis.setAnalysisId(rs.getInt(Analysis.ANALYSIS_ID));
        analysis.setAnalysisName(rs.getString(Analysis.ANALYSIS_NAME));
        analysis.setStratum1Name(rs.getString(Analysis.STRATUM_1_NAME));
        analysis.setStratum2Name(rs.getString(Analysis.STRATUM_2_NAME));
        analysis.setStratum3Name(rs.getString(Analysis.STRATUM_3_NAME));
        analysis.setStratum4Name(rs.getString(Analysis.STRATUM_4_NAME));
        analysis.setStratum5Name(rs.getString(Analysis.STRATUM_5_NAME));
        analysis.setAnalysisType(rs.getString(Analysis.ANALYSIS_TYPE));
    }
    
    /**
     * Returns all cohort analyses in the results/OHDSI schema
     * 
     * @return List of all cohort analyses
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Analysis> getCohortAnalyses() {
        
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalyses.sql");
        sql = SqlRender.renderSql(sql, new String[] { "resultsSchema" }, new String[] { this.getOhdsiSchema() });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect(), SessionUtils.sessionId(), getOhdsiSchema());
        
        return getJdbcTemplate().query(sql, this.analysisMapper);
    }
    
    /**
     * Returns all cohort analyses in the results/OHDSI schema for the given cohort_definition_id
     * 
     * @return List of all cohort analyses and their statuses for the given cohort_defintion_id
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(@PathParam("id") final int id) {
        
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohort.sql");
        sql = SqlRender.renderSql(
            sql,
            new String[] { "resultsSchema", "cohortDefinitionId" },
            new String[] { this.getOhdsiSchema(), String.valueOf(id) });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect(), SessionUtils.sessionId(), getOhdsiSchema());
        
        return getJdbcTemplate().query(sql, this.cohortAnalysisMapper);
    }
    
    /**
     * Returns the summary for the cohort
     * 
     * @param id - the cohort_defintion id
     * @return Summary which includes the base cohort_definition, the cohort analyses list and their statuses for this cohort, and a base
     * set of common cohort results that may or may not yet have been ran
     * 
     */
    @GET
    @Path("/{id}/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public CohortSummary getCohortSummary(@PathParam("id") final int id) {
        
        CohortSummary summary = new CohortSummary();
        summary.setCohortDefinition(this.definitionService.getCohortDefinition(id));
        summary.setAnalyses(this.getCohortAnalysesForCohortDefinition(id));
        
        // total patients
        List<Map<String, String>> cohortSize = this.resultsService.getCohortResultsRaw(id, "cohortSpecific", "cohortSize", null, null);
        if (cohortSize != null && cohortSize.size() > 0) {
            summary.setTotalPatients(cohortSize.get(0).get("NUM_PERSONS"));
        }
        
        List<Map<String, String>> ageAtIndexDistribution = this.resultsService.getCohortResultsRaw(id, "cohortSpecific", "ageAtIndexDistribution", null, null);
        if (ageAtIndexDistribution != null && ageAtIndexDistribution.size() > 0) {
        	summary.setMeanAge(ageAtIndexDistribution.get(0).get("AVG_VALUE"));
        }
        
        // TODO mean obs period

        CohortDashboard dashboard = this.resultsService.getDashboard(id, null, null, true);
        if (dashboard != null) {
        	summary.setGenderDistribution(dashboard.getGender());
        	summary.setAgeDistribution(dashboard.getAgeAtFirstObservation());
        }
        
        return summary;
    }
    
    /**
     * Generates a preview of the cohort analysis SQL to be ran for the Cohort Analysis Job
     * 
     * @param task - the CohortAnalysisTask, be sure to have a least one analysis_id and one cohort_definition id
     * @return - SQL for the given CohortAnalysisTask translated and rendered to the current dialect
     */
    @POST
    @Path("/preview")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getRunCohortAnalysisSql(CohortAnalysisTask task) {
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/runHeraclesAnalyses.sql");
        
        String cohortDefinitionIds = (task.getCohortDefinitionIds() == null ? "" : Joiner.on(",").join(
            task.getCohortDefinitionIds()));
        String analysisIds = (task.getAnalysisIds() == null ? "" : Joiner.on(",").join(task.getAnalysisIds()));
        String conditionIds = (task.getConditionConceptIds() == null ? "" : Joiner.on(",").join(
            task.getConditionConceptIds()));
        String drugIds = (task.getDrugConceptIds() == null ? "" : Joiner.on(",").join(task.getDrugConceptIds()));
        String procedureIds = (task.getProcedureConceptIds() == null ? "" : Joiner.on(",").join(
            task.getProcedureConceptIds()));
        String observationIds = (task.getObservationConceptIds() == null ? "" : Joiner.on(",").join(
            task.getObservationConceptIds()));
        String measurementIds = (task.getMeasurementConceptIds() == null ? "" : Joiner.on(",").join(
            task.getMeasurementConceptIds()));
        
        String[] params = new String[] { "CDM_schema", "results_schema", "source_name",
                "smallcellcount", "runHERACLESHeel", "CDM_version", "cohort_definition_id", "list_of_analysis_ids",
                "condition_concept_ids", "drug_concept_ids", "procedure_concept_ids", "observation_concept_ids",
                "measurement_concept_ids" };
        String[] values = new String[] { this.getCdmSchema(), this.getOhdsiSchema(), this.getSourceName(), 
        		String.valueOf(task.getSmallCellCount()),
                String.valueOf(task.runHeraclesHeel()).toUpperCase(), this.getCdmVersion(), cohortDefinitionIds,
                analysisIds, conditionIds, drugIds, procedureIds, observationIds, measurementIds };
        sql = SqlRender.renderSql(sql, params, values);
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect(), SessionUtils.sessionId(), getOhdsiSchema());
        
        return sql;
    }
    
    /**
     * Generates a preview of the cohort analysis SQL to be ran for the Cohort Analysis Job to an 
     * array of strings, so that it can be used in batch mode.
     * 
     * @param task
     * @return
     */
    public String[] getRunCohortAnalysisSqlBatch(CohortAnalysisTask task) {
    	if (task != null) {
    		String sql = this.getRunCohortAnalysisSql(task);
    		String[] stmts = null;
    		if (log.isDebugEnabled()) {
    			
    			stmts = SqlSplit.splitSql(sql);
                for (int x = 0; x < stmts.length; x++) {
                    log.debug(String.format("Split SQL %s : %s", x, stmts[x]));
                }
            }
    		return stmts;
    	}
    	return null;
    }
    
    /**
     * Queues up a cohort analysis task, that generates and translates SQL for the given
     * cohort definitions, analysis ids and concept ids
     * 
     * @param task - the Cohort Analysis task to be ran
     * @return information about the Cohort Analysis Job
     * @throws Exception
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource queueCohortAnalysisJob(CohortAnalysisTask task) throws Exception {
        if (task == null) {
            return null;
        }
        JobParametersBuilder builder = new JobParametersBuilder();
        String paramPrefixCD = "cohortDefinitionId:";
        for(String cd : task.getCohortDefinitionIds()){
            builder.addString(paramPrefixCD + cd, cd);
        }
        //TODO consider analysisId
        final String taskString = task.toString();
        final JobParameters jobParameters = builder.toJobParameters();
        String[] sql = this.getRunCohortAnalysisSqlBatch(task);
        log.info(String.format("Beginning run for cohort analysis task: \n %s", taskString));
        CohortAnalysisTasklet tasklet = new CohortAnalysisTasklet(sql, getJdbcTemplate(), getTransactionTemplate());
        
        return this.jobTemplate.launchTasklet("cohortAnalysisJob", "cohortAnalysisStep", tasklet, jobParameters);
    }
}
