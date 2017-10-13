/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.ohdsi.circe.helper.ResourceHelper;
import org.springframework.stereotype.Component;
import org.ohdsi.featureExtraction.FeatureExtraction;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.ohdsi.webapi.cohortfeatures.GenerateCohortFeaturesTasklet;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author asena5
 * @author alondhe2
 */
@Path("/featureextraction/")
@Component
public class FeatureExtractionService extends AbstractDaoService {

	private final String QUERY_COVARIATE_DIST = ResourceHelper.GetResourceAsString("/resources/cohortfeatures/sql/queryCovariateDist.sql");
	private final String QUERY_COVARIATE_DIST_VOCAB = ResourceHelper.GetResourceAsString("/resources/cohortfeatures/sql/queryCovariateDistVocab.sql");
	private final String QUERY_COVARIATE_STATS = ResourceHelper.GetResourceAsString("/resources/cohortfeatures/sql/queryCovariateStats.sql");
	private final String QUERY_COVARIATE_STATS_VOCAB = ResourceHelper.GetResourceAsString("/resources/cohortfeatures/sql/queryCovariateStatsVocab.sql");

	private final Pattern timeWindowPattern = Pattern.compile("(LongTerm|MediumTerm|ShortTerm|AnyTimePrior|Overlapping)");
	
	@Autowired
	private JobBuilderFactory jobBuilders;

	@Autowired
	private StepBuilderFactory stepBuilders;

	@Autowired
	private JobTemplate jobTemplate;

	public static class PrevalenceStat {

		public long covariateId;
		public String covariateName;
		public long analysisId;
		public String analysisName;
		public String domainId;
		public String timeWindow;
		public long conceptId;
		public String conceptName;
		public long countValue;
		public BigDecimal statValue;
		public long distance = 0;
	}

	public static class DistributionStat {

		public long covariateId;
		public String covariateName;
		public long analysisId;
		public String analysisName;
		public String domainId;
		public String timeWindow;
		public long conceptId;
		public long countValue;
		public BigDecimal avgValue;
		public BigDecimal stdevValue;
		public long minValue;
		public long p10Value;
		public long p25Value;
		public long medianValue;
		public long p75Value;
		public long p90Value;
		public long maxValue;
		public long distance = 0;
	}

	private List<String> buildCriteriaClauses(String searchTerm, List<String> analysisIds, List<String> timeWindows, List<String> domains) {
		ArrayList<String> clauses = new ArrayList<>();

		if (searchTerm != null && searchTerm.length() > 0) {
			clauses.add(String.format("lower(fr.covariate_name) like '%%%s%%'", searchTerm));
		}

		if (analysisIds != null && analysisIds.size() > 0) {
			ArrayList<String> ids = new ArrayList<>();
			ArrayList<String> ranges = new ArrayList<>();

			analysisIds.stream().map((analysisIdExpr) -> analysisIdExpr.split(":")).forEachOrdered((parsedIds) -> {
				if (parsedIds.length > 1) {
					ranges.add(String.format("(ar.analysis_id >= %s and ar.analysis_id <= %s)", parsedIds[0], parsedIds[1]));
				} else {
					ids.add(parsedIds[0]);
				}
			});

			String idClause = "";
			if (ids.size() > 0) {
				idClause = String.format("ar.analysis_id in (%s)", StringUtils.join(ids, ","));
			}

			if (ranges.size() > 0) {
				idClause += (idClause.length() > 0 ? " OR " : "") + StringUtils.join(ranges, " OR ");
			}

			clauses.add("(" + idClause + ")");
		}

		if (timeWindows != null && timeWindows.size() > 0) {
			ArrayList<String> timeWindowClauses = new ArrayList<>();
			timeWindows.forEach((timeWindow) -> {
				timeWindowClauses.add(String.format("ar.analysis_name like '%%%s'", timeWindow));
			});
			clauses.add("(" + StringUtils.join(timeWindowClauses, " OR ") + ")");
		}

		if (domains != null && domains.size() > 0) {
			ArrayList<String> domainClauses = new ArrayList<>();
			domains.forEach((domain) -> {
				if (domain.toLowerCase().equals("null")) {
					domainClauses.add("ar.domain_id is null");
				} else {
					domainClauses.add(String.format("lower(ar.domain_id) = lower('%s')", domain));
				}
			});
			clauses.add("(" + StringUtils.join(domainClauses, " OR ") + ")");
		}

		return clauses;
	}

	private String getTimeWindow(String analysisName)
	{
		if (analysisName.endsWith("LongTerm")) return "Long Term";
		if (analysisName.endsWith("MediumTerm")) return "Medium Term";
		if (analysisName.endsWith("ShortTerm")) return "Short Term";
		if (analysisName.endsWith("AnyTimePrior")) return "Any Time Prior";
		if (analysisName.endsWith("Overlapping")) return "Overlapping";
		
		return "None";	
	}
	
	private String getAnalysisName(String analysisName, String domainId)
	{
		String finalName = analysisName.replaceAll(timeWindowPattern.pattern(), "");
		finalName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(finalName), " ");
		if (domainId != null && finalName.startsWith(domainId) && (finalName.length() > domainId.length()))
			finalName = finalName.substring(domainId.length() + 1);
		return finalName;
	}
	
	@GET
	@Path("query/prevalence/{cohortId}/{sourceKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PrevalenceStat> getCohortFeaturePrevalenceStats(
		@PathParam("cohortId") final long cohortId,
		@PathParam("sourceKey") final String sourceKey,
		@QueryParam("searchTerm") final String searchTerm,
		@QueryParam("analysisId") final List<String> analysisIds,
		@QueryParam("timeWindow") final List<String> timeWindows,
		@QueryParam("domain") final List<String> domains
	) {
		String translatedSql;
		List<String> criteriaClauses = buildCriteriaClauses(searchTerm, analysisIds, timeWindows, domains);

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String cdmSchema = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
		
		String categoricalQuery = SqlRender.renderSql(
			QUERY_COVARIATE_STATS,
			new String[]{"cdm_database_schema", "cdm_results_schema", "cohort_definition_id", "criteria_clauses"},
			new String[]{cdmSchema, resultsSchema, Long.toString(cohortId), criteriaClauses.isEmpty() ? "" : " AND\n" + StringUtils.join(criteriaClauses, "\n AND ")}
		);

		translatedSql = SqlTranslate.translateSql(categoricalQuery, source.getSourceDialect(), SessionUtils.sessionId(), resultsSchema);
		List<PrevalenceStat> prevalenceStats = this.getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
			PrevalenceStat mappedRow = new PrevalenceStat() {
				{
					covariateId = rs.getLong("covariate_id");
					covariateName = rs.getString("covariate_name");
					analysisId = rs.getLong("analysis_id");
					analysisName = getAnalysisName( rs.getString("analysis_name"), rs.getString("domain_id"));
					domainId = rs.getString("domain_id");
					timeWindow = getTimeWindow(rs.getString("analysis_name"));
					conceptId = rs.getLong("concept_id");
					conceptName = conceptId != 0 ? rs.getString("concept_name") : covariateName;
					countValue = rs.getLong("count_value");
					statValue = new BigDecimal(rs.getDouble("stat_value")).setScale(5, RoundingMode.DOWN);
				}
			};
			return mappedRow;
		});

		return prevalenceStats;
	}

	@GET
	@Path("query/distributions/{cohortId}/{sourceKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DistributionStat> getCohortFeatureDistributionStats(
		@PathParam("cohortId") final long cohortId,
		@PathParam("sourceKey") final String sourceKey,
		@QueryParam("searchTerm") final String searchTerm,
		@QueryParam("analysisId") final List<String> analysisIds,
		@QueryParam("timeWindow") final List<String> timeWindows,
		@QueryParam("domain") final List<String> domains
	) {

		List<String> criteriaClauses = buildCriteriaClauses(searchTerm, analysisIds, timeWindows, domains);

		Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String cdmSchema = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

		String continuousQuery = SqlRender.renderSql(
			QUERY_COVARIATE_DIST,
			new String[]{"cdm_database_schema", "cdm_results_schema", "cohort_definition_id", "criteria_clauses"},
			new String[]{cdmSchema, resultsSchema, Long.toString(cohortId), criteriaClauses.isEmpty() ? "" : " AND\n" + StringUtils.join(criteriaClauses, "\n AND ")}
		);

		String translatedSql = SqlTranslate.translateSql(continuousQuery, source.getSourceDialect(), SessionUtils.sessionId(), resultsSchema);
		List<DistributionStat> distStats = this.getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
			DistributionStat mappedRow = new DistributionStat() {
				{
					covariateId = rs.getLong("covariate_id");
					covariateName = rs.getString("covariate_name");
					analysisId = rs.getLong("analysis_id");
					analysisName = getAnalysisName( rs.getString("analysis_name"), rs.getString("domain_id"));
					domainId = rs.getString("domain_id");
					timeWindow = getTimeWindow(rs.getString("analysis_name"));
					conceptId = rs.getLong("concept_id");
					countValue = rs.getLong("count_value");
					avgValue = new BigDecimal(rs.getDouble("average_value")).setScale(5, RoundingMode.DOWN);
					stdevValue = new BigDecimal(rs.getDouble("standard_deviation")).setScale(5, RoundingMode.DOWN);
					minValue = rs.getLong("min_value");
					p10Value = rs.getLong("p10_value");
					p25Value = rs.getLong("p25_value");
					medianValue = rs.getLong("median_value");
					p75Value = rs.getLong("p75_value");
					p90Value = rs.getLong("p90_value");
					maxValue = rs.getLong("max_value");
				}
			};
			return mappedRow;
		});
		return distStats;
	}

	@GET
	@Path("explore/prevalence/{cohortId}/{sourceKey}/{covariateId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PrevalenceStat> getCohortFeaturePrevalenceStatsByVocab(
		@PathParam("cohortId") final long cohortId,
		@PathParam("sourceKey") final String sourceKey,
		@PathParam("covariateId") final long covariateId
	) {
		Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String cdmSchema = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

		String categoricalQuery = SqlRender.renderSql(
			QUERY_COVARIATE_STATS_VOCAB,
			new String[]{"cdm_database_schema", "cdm_results_schema", "cohort_definition_id", "covariate_id"},
			new String[]{cdmSchema, resultsSchema, Long.toString(cohortId), Long.toString(covariateId)}
		);

		String translatedSql = SqlTranslate.translateSql(categoricalQuery, source.getSourceDialect(), SessionUtils.sessionId(), resultsSchema);
		List<PrevalenceStat> prevalenceStats = this.getSourceJdbcTemplate(source).query(translatedSql, (rs, rowNum) -> {
			PrevalenceStat mappedRow = new PrevalenceStat() {
				{
					covariateId = rs.getLong("covariate_id");
					covariateName = rs.getString("covariate_name");
					analysisId = rs.getLong("analysis_id");
					analysisName = getAnalysisName( rs.getString("analysis_name"), rs.getString("domain_id"));
					domainId = rs.getString("domain_id");
					timeWindow = getTimeWindow(rs.getString("analysis_name"));
					conceptId = rs.getLong("concept_id");
					conceptName = rs.getString("concept_name");
					countValue = rs.getLong("count_value");
					statValue = new BigDecimal(rs.getDouble("stat_value")).setScale(5, RoundingMode.DOWN);
					distance = rs.getLong("min_levels_of_separation");
				}
			};
			return mappedRow;
		});

		return prevalenceStats;
	}
	
	@GET
	@Path("/generate/{sourceKey}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JobExecutionResource generateFeatures(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

		DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
		requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);

		this.getTransactionTemplate().getTransactionManager().commit(initStatus);
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString("jobName", "generating features for cohort " + id + " : " + source.getSourceName()
			+ " (" + source.getSourceKey() + ")");
		builder.addString("cdm_database_schema", cdmTableQualifier);
		builder.addString("results_database_schema", resultsTableQualifier);
		builder.addString("target_dialect", source.getSourceDialect());
		builder.addString("cohort_definition_id", ("" + id));
		builder.addString("source_id", ("" + source.getSourceId()));

		final JobParameters jobParameters = builder.toJobParameters();

		log.info(String.format("Beginning generate cohort features for cohort definition id: \n %s", "" + id));

		GenerateCohortFeaturesTasklet generateCohortFeaturesTasklet
			= new GenerateCohortFeaturesTasklet(getSourceJdbcTemplate(source), getTransactionTemplate());

		Step generateCohortFeaturesStep = stepBuilders.get("cohortFeatures.generateFeatures")
			.tasklet(generateCohortFeaturesTasklet)
			.build();

		Job generateCohortFeaturesJob = jobBuilders.get("generateFeatures")
			.start(generateCohortFeaturesStep)
			.build();

		JobExecutionResource jobExec = this.jobTemplate.launch(generateCohortFeaturesJob, jobParameters);
		return jobExec;
	}

	@GET
	@Path("/generatesql/{sourceKey}/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String GenerateFeaturesSql(@PathParam("sourceKey") String sourceKey, @PathParam("id") String cohortId) {
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

		FeatureExtraction.init(null);
		String settings = FeatureExtraction.getDefaultPrespecAnalyses();
		String sqlJson = FeatureExtraction.createSql(settings, true, resultsTableQualifier + ".cohort",
			"subject_id", Integer.parseInt(cohortId), tableQualifier);

		JSONObject jsonObject = new JSONObject(sqlJson);

		String insertStr = "insert into @resultsDatabaseSchema.{0} \r\n {1};";
		String cohortWrapper = "select {0} as cohort_definition_id, {1} from ({2})";
		String columns = "";
		StringJoiner joiner = new StringJoiner("\r\n ---- \r\n");

		//      String clearSql = "delete from @resultsDatabaseSchema.{0} where cohort_definition_id = {1};";
		//      String[] tables = new String[] { "cohort_features", "cohort_features_dist", "cohort_features_ref", "cohort_features_analysis_ref" };
		//      
		//      for (String table : tables)
		//          joiner.add(MessageFormat.format(clearSql, table, cohortId));
		joiner.add(jsonObject.getString("sqlConstruction"));

		columns = "cohort_definition_id, covariate_id, sum_value, average_value";
		joiner.add(MessageFormat.format(insertStr,
			"cohort_features",
			MessageFormat.format(cohortWrapper, cohortId, columns,
				StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatures"), ";"))));

		columns = "covariate_id, count_value, min_value, max_value, average_value, "
			+ "standard_deviation, median_value, p10_value, p25_value, p75_value, p90_value";
		joiner.add(MessageFormat.format(insertStr,
			"cohort_features_dist",
			MessageFormat.format(cohortWrapper, cohortId, columns,
				StringUtils.stripEnd(jsonObject.getString("sqlQueryContinuousFeatures"), ";"))));

		columns = "covariate_id, covariate_name, analysis_id, concept_id";
		joiner.add(MessageFormat.format(insertStr,
			"cohort_features_ref",
			MessageFormat.format(cohortWrapper, cohortId, columns,
				StringUtils.stripEnd(jsonObject.getString("sqlQueryFeatureRef"), ";"))));

		columns = "analysis_id, analysis_name, domain_id, start_day, end_day, is_binary, missing_means_zero";
		joiner.add(MessageFormat.format(insertStr,
			"cohort_features_analysis_ref",
			MessageFormat.format(cohortWrapper, cohortId, columns,
				StringUtils.stripEnd(jsonObject.getString("sqlQueryAnalysisRef"), ";"))));

		joiner.add(jsonObject.getString("sqlCleanup"));

		String fullSql = SqlRender.renderSql(joiner.toString(), new String[]{"resultsDatabaseSchema"}, new String[]{resultsTableQualifier});
		fullSql = SqlTranslate.translateSql(fullSql, source.getSourceDialect());

		return fullSql;
	}
}
