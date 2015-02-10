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
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.CohortAnalysis;
import org.ohdsi.webapi.cohortresults.CohortAnalysisTask;
import org.ohdsi.webapi.cohortresults.CohortAnalysisTasklet;
import org.ohdsi.webapi.cohortresults.CohortSummary;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.model.results.Analysis;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * 
 * Services related to running Heracles analyses
 *
 */
@Path("/cohortanalysis/")
@Component
public class CohortAnalysisService extends AbstractDaoService {
	
    @Autowired
    JobTemplate jobTemplate;

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
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Analysis> getCohortAnalyses() {
        
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalyses.sql");
        sql = SqlRender.renderSql(sql, new String[] { "resultsSchema" }, new String[] { this.getOhdsiSchema() });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
        
        return getJdbcTemplate().query(sql, this.analysisMapper);
    }
    
    /**
     * Returns all cohort analyses in the results/OHDSI schema
     * 
     * @return
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(@PathParam("id") final int id) {
        
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohort.sql");
        sql = SqlRender.renderSql(sql, new String[] { "resultsSchema", "heraclesResultsTable", "heraclesResultsDistTable", "cohortDefinitionId" }, 
        		new String[] { this.getOhdsiSchema(), this.getHeraclesResultsTable(), this.getHeraclesResultsDistTable(), String.valueOf(id) });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
        
        return getJdbcTemplate().query(sql, this.cohortAnalysisMapper);
    }
    
    /**
     * Returns the summary for the cohort
     * 
     * @return
     */
    @GET
    @Path("/{id}/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public CohortSummary getCohortSummary(@PathParam("id") final int id) {
        
    	CohortSummary summary = new CohortSummary();
    	summary.setDefinition(this.definitionService.getCohortDefinition(id));
    	summary.setAnalyses(this.getCohortAnalysesForCohortDefinition(id));
    	
    	// total patients
    	List<Map<String, String>> cohortSize = this.resultsService.getCohortResults(id, "cohortSize", null, null);
    	if (cohortSize != null && cohortSize.size() > 0) {
    		summary.setTotalPatients(cohortSize.get(0).get("NUM_PERSONS"));
    	}
    	
    	// TODO mean age
    	
    	// TODO mean obs period
    	
    	// gender distribution
    	summary.setGenderDistribution(this.resultsService.getCohortResults(id, "gender", null, null));
    	
    	// TODO age distribution
    	
    	return summary;
    }
    
   
    /**
     * Generates a preview of the cohort analysis SQL to be ran
     * 
     * @param task
     * @return
     */
    @POST
    @Path("/preview")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getRunCohortAnalysisSql(CohortAnalysisTask task) {
        String sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/runHeraclesAnalyses.sql");

        String cohortDefinitionIds = (task.getCohortDefinitionId() == null ? "" : Joiner.on(",").join(task.getCohortDefinitionId()));
        String analysisIds = (task.getAnalysisId() == null ? "" : Joiner.on(",").join(task.getAnalysisId()));
        String conditionIds = (task.getConditionConceptIds() == null ? "" : Joiner.on(",").join(task.getConditionConceptIds()));
        String drugIds = (task.getDrugConceptIds() == null ? "" : Joiner.on(",").join(task.getDrugConceptIds()));
        String procedureIds = (task.getProcedureConceptIds() == null ? "" : Joiner.on(",").join(task.getProcedureConceptIds()));
        String observationIds = (task.getObservationConceptIds()  == null ? "" : Joiner.on(",").join(task.getObservationConceptIds()));
        String measurementIds = (task.getMeasurementConceptIds() == null ? "" : Joiner.on(",").join(task.getMeasurementConceptIds()));
        
        String[] params = new String[] { "CDM_schema", "results_schema", "cohort_schema", "cohort_table", "source_name",
        		"smallcellcount", "runHERACLESHeel", "CDM_version", 
        		"cohort_definition_id", "list_of_analysis_ids", "condition_concept_ids", 
        		"drug_concept_ids", "procedure_concept_ids", "observation_concept_ids", "measurement_concept_ids" };
        String[] values = new String[] { this.getCdmSchema(), this.getOhdsiSchema(), this.getCohortSchema(), this.getCohortTable(), this.getSourceName(),
        		String.valueOf(task.getSmallCellCount()), String.valueOf(task.runHeraclesHeel()).toUpperCase(), this.getCdmVersion(),
        		cohortDefinitionIds, analysisIds, conditionIds,
        		drugIds, procedureIds, observationIds, measurementIds };
        sql = SqlRender.renderSql(sql, params, values);
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
        
    	return sql;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource queueCohortAnalysisJob(CohortAnalysisTask task) throws Exception {
        final JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()).toJobParameters();
    	if (task == null) {
    		return null;
    	}
    	String sql = this.getRunCohortAnalysisSql(task);
    	CohortAnalysisTasklet tasklet = new CohortAnalysisTasklet(task, sql, this.getJdbcTemplate());
    	
        return this.jobTemplate.launchTasklet("cohortAnalysisJob", "cohortAnalysisStep", tasklet, jobParameters);
    }
}


