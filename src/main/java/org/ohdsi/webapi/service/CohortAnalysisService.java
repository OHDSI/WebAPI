package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortanalysis.*;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortresults.VisualizationDataRepository;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.model.results.Analysis;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * REST Services related to running 
 * cohort analysis (a.k.a Heracles) analyses. 
 * More information on the Heracles project
 * can be found at {@link https://www.ohdsi.org/web/wiki/doku.php?id=documentation:software:heracles}.
 * The implementation found in WebAPI represents a migration of the functionality
 * from the stand-alone HERACLES application to integrate it into WebAPI and
 * ATLAS.
 * 
 * @summary Cohort Analysis (a.k.a Heracles)
 */
@Path("/cohortanalysis/")
@Component
public class CohortAnalysisService extends AbstractDaoService implements GeneratesNotification {

	public static final String NAME = "cohortAnalysisJob";
	@Value("${heracles.smallcellcount}")
	private String smallCellCount;
	
	private final JobTemplate jobTemplate;

	private final CohortDefinitionService definitionService;
	
  private final CohortDefinitionRepository cohortDefinitionRepository;
	
	private final VisualizationDataRepository visualizationDataRepository;

	private final HeraclesQueryBuilder heraclesQueryBuilder;

	private ObjectMapper objectMapper;

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

  public CohortAnalysisService(JobTemplate jobTemplate,
                               CohortDefinitionService definitionService,
                               CohortDefinitionRepository cohortDefinitionRepository,
                               VisualizationDataRepository visualizationDataRepository,
                               ObjectMapper objectMapper,
                               HeraclesQueryBuilder heraclesQueryBuilder) {

    this.jobTemplate = jobTemplate;
    this.definitionService = definitionService;
    this.cohortDefinitionRepository = cohortDefinitionRepository;
    this.visualizationDataRepository = visualizationDataRepository;
    this.objectMapper = objectMapper;
    this.heraclesQueryBuilder = heraclesQueryBuilder;
  }

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
	 * Returns all cohort analyses in the WebAPI database
         * 
	 * @summary Get all cohort analyses
	 * @return List of all cohort analyses
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Analysis> getCohortAnalyses() {
		String sqlPath = "/resources/cohortanalysis/sql/getCohortAnalyses.sql";
		String search = "ohdsi_database_schema";
		String replace = getOhdsiSchema();
		PreparedStatementRenderer psr = new PreparedStatementRenderer(null, sqlPath, search, replace);
		return getJdbcTemplate().query(psr.getSql(), psr.getSetter(), this.analysisMapper);
	}

        /**
         * Returns all cohort analyses in the WebAPI database
         * for the given cohort_definition_id
         * 
         * @summary Get cohort analyses by cohort ID
         * @param id The cohort definition identifier
         * @return List of all cohort analyses and their statuses 
         * for the given cohort_definition_id
         */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(@PathParam("id") final int id) {
        String sqlPath = "/resources/cohortanalysis/sql/getCohortAnalysesForCohort.sql";
        String tqName = "ohdsi_database_schema";
        String tqValue = getOhdsiSchema();
        PreparedStatementRenderer psr = new PreparedStatementRenderer(null, sqlPath, tqName, tqValue, "cohortDefinitionId", whitelist(id), SessionUtils.sessionId());
        return getJdbcTemplate().query(psr.getSql(), psr.getSetter(), this.cohortAnalysisMapper);
	}

    /**
     * Returns the summary for the cohort
     * 
     * @summary Cohort analysis summary
     * @param id - the cohort_definition id
     * @return Summary which includes the base cohort_definition, the cohort analyses list and their
     *         statuses for this cohort, and a base set of common cohort results that may or may not
     *         yet have been ran
     */
    @GET
    @Path("/{id}/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public CohortSummary getCohortSummary(@PathParam("id") final int id) {

        CohortSummary summary = new CohortSummary();
        try {
            summary.setCohortDefinition(this.definitionService.getCohortDefinition(whitelist(id)));
            summary.setAnalyses(this.getCohortAnalysesForCohortDefinition(id));
        } catch (Exception e) {
            log.error("unable to get cohort summary", e);
        }

        return summary;
    }

        /**
	 * Generates a preview of the cohort analysis SQL used to run 
         * the Cohort Analysis Job
	 *
         * @summary Cohort analysis SQL preview
	 * @param task - the CohortAnalysisTask, be sure to have a least one
	 * analysis_id and one cohort_definition id
	 * @return - SQL for the given CohortAnalysisTask translated and rendered to
	 * the current dialect
	 */
	@POST
	@Path("/preview")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getRunCohortAnalysisSql(CohortAnalysisTask task) {
		task.setSmallCellCount(Integer.parseInt(this.smallCellCount));
		return heraclesQueryBuilder.buildHeraclesAnalysisQuery(task);
	}

	/**
	 * Generates a preview of the cohort analysis SQL to be ran for the Cohort
	 * Analysis Job to an array of strings, so that it can be used in batch mode.
	 *
         * @summary Run cohort analysis SQL batch
	 * @param task The CohortAnalysisTask object
	 * @return The SQL statements or NULL if there is no task specified
	 */
	public String[] getRunCohortAnalysisSqlBatch(CohortAnalysisTask task) {
		if (task != null) {
			task.setSmallCellCount(Integer.parseInt(this.smallCellCount));
			String sql = this.getRunCohortAnalysisSql(task);
			String[] stmts = null;
			if (log.isDebugEnabled()) {

				stmts = SqlSplit.splitSql(sql);
				for (int x = 0; x < stmts.length; x++) {
					log.debug("Split SQL {} : {}", x, stmts[x]);
				}
			}
			return stmts;
		}
		return null;
	}

	/**
	 * Queues up a cohort analysis task, that generates and translates SQL for the
	 * given cohort definitions, analysis ids and concept ids
	 *
         * @summary Queue cohort analysis job
	 * @param task The cohort analysis task to be ran
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
		
		// source key comes from the client, we look it up here and hand it off to the tasklet
		Source source = getSourceRepository().findBySourceKey(task.getSourceKey());
		task.setSource(source);
	
		task.setSmallCellCount(Integer.parseInt(this.smallCellCount));
		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("sourceKey", source.getSourceKey());
		builder.addString("cohortDefinitionIds", limitJobParams(Joiner.on(",").join(task.getCohortDefinitionIds())));
		builder.addString("analysisIds", limitJobParams(Joiner.on(",").join(task.getAnalysisIds())));
		if (task.getConditionConceptIds() != null && task.getConditionConceptIds().size() > 0) {
			builder.addString("conditionIds", limitJobParams(Joiner.on(",").join(task.getConditionConceptIds())));
		}
		if (task.getDrugConceptIds() != null && task.getDrugConceptIds().size() > 0) {
			builder.addString("drugIds", limitJobParams(Joiner.on(",").join(task.getDrugConceptIds())));
		}
		if (task.getMeasurementConceptIds() != null && task.getMeasurementConceptIds().size() > 0) {
			builder.addString("measurementIds", limitJobParams(Joiner.on(",").join(task.getMeasurementConceptIds())));
		}
		if (task.getObservationConceptIds() != null && task.getObservationConceptIds().size() > 0) {
			builder.addString("observationIds", limitJobParams(Joiner.on(",").join(task.getObservationConceptIds())));
		}
		if (task.getProcedureConceptIds() != null && task.getProcedureConceptIds().size() > 0) {
			builder.addString("procedureIds", limitJobParams(Joiner.on(",").join(task.getProcedureConceptIds())));
		}
		if (task.isRunHeraclesHeel()) {
			builder.addString("heraclesHeel", "true");
		}
		if (task.isCohortPeriodOnly()) {
			builder.addString("cohortPeriodOnly", "true");
		}
		
		if (!StringUtils.isEmpty(task.getJobName())) {
			builder.addString("jobName", limitJobParams(task.getJobName()));
		}

		// clear analysis IDs from the generated set
		this.getTransactionTemplateRequiresNew().execute(status -> { 
			CohortDefinition cohortDef = this.cohortDefinitionRepository.findOne(Integer.parseInt(task.getCohortDefinitionIds().get(0)));
			CohortAnalysisGenerationInfo info = cohortDef.getCohortAnalysisGenerationInfoList().stream()
				.filter(a -> a.getSourceId() == task.getSource().getSourceId())
				.findFirst()
				.orElseGet(() -> {
          CohortAnalysisGenerationInfo genInfo = new CohortAnalysisGenerationInfo();
          genInfo.setSourceId(task.getSource().getSourceId());
          genInfo.setCohortDefinition(cohortDef);
          cohortDef.getCohortAnalysisGenerationInfoList().add(genInfo);
          return genInfo;
        });
			List<Integer> analysisList = task.getAnalysisIds().stream().map(Integer::parseInt).collect(Collectors.toList());
			info.getAnalysisIds().removeAll(analysisList);
			info.setLastExecution(Calendar.getInstance().getTime());
			info.setProgress(0);
			this.cohortDefinitionRepository.save(cohortDef);
			return null;
		});

		//TODO consider analysisId
		final String taskString = task.toString();
		final JobParameters jobParameters = builder.toJobParameters();
		log.info("Beginning run for cohort analysis task: {}", taskString);

		CohortAnalysisTasklet tasklet = new CohortAnalysisTasklet(task, getSourceJdbcTemplate(task.getSource()), 
				getTransactionTemplate(), getTransactionTemplateRequiresNew(), this.getSourceDialect(), this.visualizationDataRepository,
				this.cohortDefinitionRepository, objectMapper, heraclesQueryBuilder);

		return this.jobTemplate.launchTasklet(NAME, "cohortAnalysisStep", tasklet, jobParameters);
	}

	@Override
	public String getJobName() {
		return NAME;
	}

	@Override
	public String getExecutionFoldingKey() {
		return "analysisIds";
	}

	private String limitJobParams(String param) {
		if (param.length() >= 250) {
			return param.substring(0, 245) + "...";
		}
		return param;
	}
}
