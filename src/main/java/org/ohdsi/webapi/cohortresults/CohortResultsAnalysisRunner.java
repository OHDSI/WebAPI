package org.ohdsi.webapi.cohortresults;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortresults.mapper.*;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

public class CohortResultsAnalysisRunner {

	public static final String COHORT_SPECIFIC = "cohort_specific";
	public static final String COHORT_SPECIFIC_CONDITION_DRILLDOWN = "cohort_specific_condition_drilldown";
	public static final String COHORT_SPECIFIC_DRUG_DRILLDOWN = "cohort_specific_drug_drilldown";
	public static final String COHORT_SPECIFIC_PROCEDURE_DRILLDOWN = "cohort_specific_procedure_drilldown";
	public static final String COHORT_SPECIFIC_TREEMAP = "cohort_specific_treemap";
	public static final String CONDITION = "condition";
	public static final String CONDITION_DRILLDOWN = "condition_drilldown";
	public static final String CONDITION_ERA = "condition_era";
	public static final String CONDITION_ERA_DRILLDOWN = "condition_era_drilldown";
	public static final String DASHBOARD = "dashboard";
	public static final String DATA_DENSITY = "data_density";
	public static final String DEATH = "death";
	public static final String DEFAULT = "default";
	public static final String DRUG = "drug";
	public static final String DRUG_DRILLDOWN = "drug_drilldown";
	public static final String DRUG_ERA = "drug_era";
	public static final String DRUG_ERA_DRILLDOWN = "drug_era_drilldown";
	public static final String DRUG_EXPOSURE = "drug_exposure";
	public static final String HERACLES_HEEL = "heracles_heel";
	public static final String MEASUREMENT = "measurement";
	public static final String MEASUREMENT_DRILLDOWN = "measurement_drilldown";
	public static final String OBSERVATION = "observation";
	public static final String OBSERVATION_DRILLDOWN = "observation_drilldown";
	public static final String OBSERVATION_PERIOD = "observation_period";
	public static final String OBSERVATION_PERIODS = "observation_periods";
	public static final String PERSON = "person";
	public static final String PROCEDURE = "procedure";
	public static final String PROCEDURE_DRILLDOWN = "procedure_drilldown";
	public static final String VISIT = "visit";
	public static final String VISIT_DRILLDOWN = "visit_drilldown";
	
	private static final Logger log = LoggerFactory.getLogger(CohortResultsAnalysisRunner.class);
	
	public static final String BASE_SQL_PATH = "/resources/cohortresults/sql";

	private static final String[] STANDARD_COLUMNS = new String[]{"cdm_database_schema",
		"ohdsi_database_schema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount"};

	private static final String[] DRILLDOWN_COLUMNS = new String[]{"cdm_database_schema",
		"ohdsi_database_schema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount", "conceptId"};

	public static final Integer MIN_COVARIATE_PERSON_COUNT = 10;
	public static final Integer MIN_INTERVAL_PERSON_COUNT = 10;

	private ObjectMapper mapper;
	private String sourceDialect;
	private VisualizationDataRepository visualizationDataRepository;

	public CohortResultsAnalysisRunner(String sourceDialect, VisualizationDataRepository visualizationDataRepository, ObjectMapper objectMapper) {
		this.sourceDialect = sourceDialect;
		this.visualizationDataRepository = visualizationDataRepository;
		mapper = objectMapper;
	}

	public List<TornadoRecord> getTornadoRecords(JdbcTemplate jdbcTemplate, final int id, Source source) {
		final String sqlPath = BASE_SQL_PATH + "/tornado/getTornadoData.sql";
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

		String[] search = new String[]{"tableQualifier"};
		String[] replace = new String[]{resultsTableQualifier};

		String[] cols = new String[]{"cohortDefinitionId"};
		Object[] colValues = new Object[]{id};

		final PreparedStatementRenderer psr =  new PreparedStatementRenderer(source, sqlPath, search, replace, cols, colValues);
		return jdbcTemplate.query(psr.getSql(), psr.getSetter(), new TornadoMapper());
	}

	public List<ProfileSampleRecord> getProfileSampleRecords(JdbcTemplate jdbcTemplate, final int id, Source source) {
		final String sqlPath = BASE_SQL_PATH + "/tornado/getProfileSamples.sql";
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

		String[] search = new String[]{"tableQualifier"};
		String[] replace = new String[]{resultsTableQualifier};

		String[] cols = new String[]{"cohortDefinitionId"};
		Object[] colValues = new Object[]{id};

		final PreparedStatementRenderer psr =  new PreparedStatementRenderer(source, sqlPath, search, replace, cols, colValues);
		return jdbcTemplate.query(psr.getSql(), psr.getSetter(), new ProfileSampleMapper());
	}

	public List<ScatterplotRecord> getCohortConditionDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, 
			boolean save) {

		final String key = COHORT_SPECIFIC_CONDITION_DRILLDOWN;
		List<ScatterplotRecord> records = new ArrayList<>();

		final PreparedStatementRenderer psr = prepareDrillDownCohortSql("firstConditionRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (psr != null) {
			records = jdbcTemplate.query(psr.getSql(), psr.getSetter(), new ScatterplotMapper());
			if (save) {
				this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}

		return records;
	}

	public CohortDataDensity getCohortDataDensity(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		CohortDataDensity data = new CohortDataDensity();
		final String key = DATA_DENSITY;
		boolean empty = true;

		PreparedStatementRenderer recordsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/recordsperperson.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (recordsPerPersonSql != null) {
			data.setRecordsPerPerson(jdbcTemplate.query(recordsPerPersonSql.getSql(), recordsPerPersonSql.getSetter(), new SeriesPerPersonMapper()));
		}
		PreparedStatementRenderer totalRecordsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/totalrecords.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (totalRecordsSql != null) {
			data.setTotalRecords(jdbcTemplate.query(totalRecordsSql.getSql(), totalRecordsSql.getSetter(), new SeriesPerPersonMapper()));
		}
		PreparedStatementRenderer conceptsPerPersonSql = renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/conceptsperperson.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conceptsPerPersonSql != null) {
			data.setConceptsPerPerson(jdbcTemplate.query(conceptsPerPersonSql.getSql(), conceptsPerPersonSql.getSetter(), new ConceptQuartileMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(data.getRecordsPerPerson())
				|| CollectionUtils.isNotEmpty(data.getTotalRecords())
				|| CollectionUtils.isNotEmpty(data.getConceptsPerPerson())) {
			empty = false;
		}

		if (!empty && save) {
			saveEntity(id, source.getSourceId(), key, data);
		}

		return data;
	}

	public CohortDeathData getCohortDeathData(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortDeathData data = new CohortDeathData();
		final String key = DEATH;
		boolean empty = true;

		List<ConceptQuartileRecord> age = null;
		PreparedStatementRenderer ageSql = renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlAgeAtDeath.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageSql != null) {
			age = jdbcTemplate.query(ageSql.getSql(), ageSql.getSetter(), new ConceptQuartileMapper());
		}
		data.setAgetAtDeath(age);

		List<ConceptCountRecord> byType = null;
		PreparedStatementRenderer byTypeSql = renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlDeathByType.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql.getSql(), byTypeSql.getSetter(), new ConceptCountMapper());
		}
		data.setDeathByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceGenderAgeSql = renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByGenderAgeYear.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql.getSql(), prevalenceGenderAgeSql.getSetter(), new ConceptDecileCountsMapper());
		}
		data.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByMonth.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		data.setPrevalenceByMonth(prevalenceByMonth);

		if (CollectionUtils.isNotEmpty(data.getAgetAtDeath())
				|| CollectionUtils.isNotEmpty(data.getDeathByType())
				|| CollectionUtils.isNotEmpty(data.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(data.getPrevalenceByMonth())) {
			empty = false;
		}
		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, data);
		}

		return data;
	}

	public List<ScatterplotRecord> getCohortDrugDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<ScatterplotRecord> records = new ArrayList<>();
		final String key = COHORT_SPECIFIC_DRUG_DRILLDOWN;

		final PreparedStatementRenderer sql = prepareDrillDownCohortSql("drugOccursRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			records = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new ScatterplotMapper());
			if (save) {
				saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}

		return records;
	}

	public List<HierarchicalConceptRecord> getCohortMeasurementResults(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		List<HierarchicalConceptRecord> res = null;
		final String key = MEASUREMENT;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/measurement/sqlMeasurementTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}
		return res;
	}

	public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortMeasurementDrilldown drilldown = new CohortMeasurementDrilldown();
		final String key = MEASUREMENT_DRILLDOWN;
		boolean empty = true;

		PreparedStatementRenderer ageAtFirstOccurrenceSql = prepareDrillDownCohortSql("sqlAgeAtFirstOccurrence", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstOccurrenceSql != null) {
			drilldown.setAgeAtFirstOccurrence(jdbcTemplate.query(ageAtFirstOccurrenceSql.getSql(), ageAtFirstOccurrenceSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlLowerLimitDistribution = prepareDrillDownCohortSql("sqlLowerLimitDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlLowerLimitDistribution != null) {
			drilldown.setLowerLimitDistribution(jdbcTemplate.query(sqlLowerLimitDistribution.getSql(), sqlLowerLimitDistribution.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlMeasurementValueDistribution = prepareDrillDownCohortSql("sqlMeasurementValueDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlMeasurementValueDistribution != null) {
			drilldown.setMeasurementValueDistribution(jdbcTemplate.query(sqlMeasurementValueDistribution.getSql(), sqlMeasurementValueDistribution.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlUpperLimitDistribution = prepareDrillDownCohortSql("sqlUpperLimitDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlUpperLimitDistribution != null) {
			drilldown.setUpperLimitDistribution(jdbcTemplate.query(sqlUpperLimitDistribution.getSql(), sqlUpperLimitDistribution.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlMeasurementsByType = prepareDrillDownCohortSql("sqlMeasurementsByType", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlMeasurementsByType != null) {
			drilldown.setMeasurementsByType(jdbcTemplate.query(sqlMeasurementsByType.getSql(), sqlMeasurementsByType.getSetter(), new ConceptObservationCountMapper()));
		}

		PreparedStatementRenderer sqlRecordsByUnit = prepareDrillDownCohortSql("sqlRecordsByUnit", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlRecordsByUnit != null) {
			drilldown.setRecordsByUnit(jdbcTemplate.query(sqlRecordsByUnit.getSql(), sqlRecordsByUnit.getSetter(), new ConceptObservationCountMapper()));
		}

		PreparedStatementRenderer sqlValuesRelativeToNorm = prepareDrillDownCohortSql("sqlValuesRelativeToNorm", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlValuesRelativeToNorm != null) {
			drilldown.setValuesRelativeToNorm(jdbcTemplate.query(sqlValuesRelativeToNorm.getSql(), sqlValuesRelativeToNorm.getSetter(), new ConceptObservationCountMapper()));
		}

		PreparedStatementRenderer sqlPrevalenceByGenderAgeYear = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlPrevalenceByGenderAgeYear != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(sqlPrevalenceByGenderAgeYear.getSql(), sqlPrevalenceByGenderAgeYear.getSetter(), new ConceptDecileMapper()));
		}

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptNameMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstOccurrence())
				|| CollectionUtils.isNotEmpty(drilldown.getLowerLimitDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getMeasurementsByType())
				|| CollectionUtils.isNotEmpty(drilldown.getMeasurementValueDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getRecordsByUnit())
				|| CollectionUtils.isNotEmpty(drilldown.getUpperLimitDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getValuesRelativeToNorm()))  {
			empty = false;
		}
		
		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, drilldown);
		}

		return drilldown;
	}

	public CohortObservationPeriod getCohortObservationPeriod(final JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortObservationPeriod obsPeriod = new CohortObservationPeriod();
		final String key = OBSERVATION_PERIOD;
		boolean empty = true;

		PreparedStatementRenderer ageAtFirstSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			obsPeriod.setAgeAtFirst(jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(), new ConceptDistributionMapper()));
		}

		PreparedStatementRenderer obsLengthSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_data.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsLengthSql != null) {
			obsPeriod.setObservationLength(jdbcTemplate.query(obsLengthSql.getSql(), obsLengthSql.getSetter(), new ConceptDistributionMapper()));
		}

		PreparedStatementRenderer obsLengthStatsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsLengthStatsSql != null) {
			obsPeriod.setObservationLengthStats(jdbcTemplate.query(obsLengthStatsSql.getSql(), obsLengthStatsSql.getSetter(), new CohortStatsMapper()));
		}

		PreparedStatementRenderer obsYearStatsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsYearStatsSql != null) {
			obsPeriod.setPersonsWithContinuousObservationsByYearStats(jdbcTemplate.query(obsYearStatsSql.getSql(), obsYearStatsSql.getSetter(), new CohortStatsMapper()));
		}

		PreparedStatementRenderer personsWithContObsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_data.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsWithContObsSql != null) {
			obsPeriod.setPersonsWithContinuousObservationsByYear(jdbcTemplate.query(personsWithContObsSql.getSql(), personsWithContObsSql.getSetter(), new ConceptDistributionMapper()));
		}

		PreparedStatementRenderer ageByGenderSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/agebygender.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageByGenderSql != null) {
			obsPeriod.setAgeByGender(jdbcTemplate.query(ageByGenderSql.getSql(), ageByGenderSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer durationByGenderSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbygender.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (durationByGenderSql != null) {
			obsPeriod.setDurationByGender(jdbcTemplate.query(durationByGenderSql.getSql(), durationByGenderSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer durationByAgeSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbyage.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (durationByAgeSql != null) {
			obsPeriod.setDurationByAgeDecile(jdbcTemplate.query(durationByAgeSql.getSql(), durationByAgeSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer cumulObsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (cumulObsSql != null) {
			obsPeriod.setCumulativeObservation(jdbcTemplate.query(cumulObsSql.getSql(), cumulObsSql.getSetter(), new CumulativeObservationMapper()));
		}

		PreparedStatementRenderer obsByMonthSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsByMonthSql != null) {
			obsPeriod.setObservedByMonth(jdbcTemplate.query(obsByMonthSql.getSql(), obsByMonthSql.getSetter(), new MonthObservationMapper()));
		}

		PreparedStatementRenderer obsPeriodsPerPersonSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/periodsperperson.sql", id, minCovariatePersonCountParam,
				minIntervalPersonCountParam, source);
		if (obsPeriodsPerPersonSql != null) {
			obsPeriod.setObservationPeriodsPerPerson(jdbcTemplate.query(obsPeriodsPerPersonSql.getSql(), obsPeriodsPerPersonSql.getSetter(), new ConceptCountMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(obsPeriod.getAgeAtFirst())
				|| CollectionUtils.isNotEmpty(obsPeriod.getAgeByGender())
				|| CollectionUtils.isNotEmpty(obsPeriod.getCumulativeObservation())
				|| CollectionUtils.isNotEmpty(obsPeriod.getDurationByAgeDecile())
				|| CollectionUtils.isNotEmpty(obsPeriod.getDurationByGender())
				|| CollectionUtils.isNotEmpty(obsPeriod.getObservationLength())
				|| CollectionUtils.isNotEmpty(obsPeriod.getObservationLengthStats())
				|| CollectionUtils.isNotEmpty(obsPeriod.getObservationPeriodsPerPerson())
				|| CollectionUtils.isNotEmpty(obsPeriod.getObservedByMonth())
				|| CollectionUtils.isNotEmpty(obsPeriod.getPersonsWithContinuousObservationsByYear())
				|| CollectionUtils.isNotEmpty(obsPeriod.getPersonsWithContinuousObservationsByYearStats())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, obsPeriod);
		}

		return obsPeriod;
	}

	public List<HierarchicalConceptRecord> getCohortObservationResults(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;

		final String key = OBSERVATION;
		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/observation/sqlObservationTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortObservationDrilldown getCohortObservationResultsDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortObservationDrilldown drilldown = new CohortObservationDrilldown();
		final String key = OBSERVATION_DRILLDOWN;
		boolean empty = true;

		PreparedStatementRenderer ageAtFirstOccurrenceSql = prepareDrillDownCohortSql("sqlAgeAtFirstOccurrence", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstOccurrenceSql != null) {
			drilldown.setAgeAtFirstOccurrence(jdbcTemplate.query(ageAtFirstOccurrenceSql.getSql(), ageAtFirstOccurrenceSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlObservationValueDistribution = prepareDrillDownCohortSql("sqlObservationValueDistribution", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlObservationValueDistribution != null) {
			drilldown.setObservationValueDistribution(jdbcTemplate.query(sqlObservationValueDistribution.getSql(), sqlObservationValueDistribution.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer sqlObservationsByType = prepareDrillDownCohortSql("sqlObservationsByType", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlObservationsByType != null) {
			drilldown.setObservationsByType(jdbcTemplate.query(sqlObservationsByType.getSql(), sqlObservationsByType.getSetter(), new ConceptObservationCountMapper()));
		}

		PreparedStatementRenderer sqlRecordsByUnit = prepareDrillDownCohortSql("sqlRecordsByUnit", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlRecordsByUnit != null) {
			drilldown.setRecordsByUnit(jdbcTemplate.query(sqlRecordsByUnit.getSql(), sqlRecordsByUnit.getSetter(), new ConceptObservationCountMapper()));
		}

		PreparedStatementRenderer sqlPrevalenceByGenderAgeYear = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlPrevalenceByGenderAgeYear != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(sqlPrevalenceByGenderAgeYear.getSql(), sqlPrevalenceByGenderAgeYear.getSetter(), new ConceptDecileMapper()));
		}

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptNameMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstOccurrence())
				|| CollectionUtils.isNotEmpty(drilldown.getLowerLimitDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getObservationsByType())
				|| CollectionUtils.isNotEmpty(drilldown.getObservationValueDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getRecordsByUnit())
				|| CollectionUtils.isNotEmpty(drilldown.getUpperLimitDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getValuesRelativeToNorm())) {
			empty = false;
		}
		
		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, drilldown);
		}
		return drilldown;

	}

	public List<ScatterplotRecord> getCohortProcedureDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<ScatterplotRecord> records = new ArrayList<>();
		final String key = COHORT_SPECIFIC_PROCEDURE_DRILLDOWN;

		final PreparedStatementRenderer sql = prepareDrillDownCohortSql("procedureOccursRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			records = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new ScatterplotMapper());
			if (save) {
				saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}
		return records;
	}

	public CohortProceduresDrillDown getCohortProceduresDrilldown(JdbcTemplate jdbcTemplate,
			final int id,
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortProceduresDrillDown drilldown = new CohortProceduresDrillDown();
		final String key = PROCEDURE_DRILLDOWN;
		boolean empty = true;
		
		List<ConceptQuartileRecord> ageAtFirst = null;
		PreparedStatementRenderer ageAtFirstSql = prepareDrillDownCohortSql("sqlAgeAtFirstOccurrence", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstOccurrence(ageAtFirst);

		List<ConceptCountRecord> byType = null;
		PreparedStatementRenderer byTypeSql = prepareDrillDownCohortSql("sqlProceduresByType", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql.getSql(), byTypeSql.getSetter(), new ConceptCountMapper());
		}
		drilldown.setProceduresByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceGenderAgeSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql.getSql(), prevalenceGenderAgeSql.getSetter(), new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstOccurrence())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getProceduresByType())) {
			empty = false;
		}

		if (!empty && save) {
			saveEntityDrilldown(id, source.getSourceId(), key, conceptId, drilldown);
		}

		return drilldown;
	}

	public CohortSpecificSummary getCohortSpecificSummary(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, 
			boolean save) {
		final String key = COHORT_SPECIFIC;
		CohortSpecificSummary summary = new CohortSpecificSummary();
		boolean empty = true;
		
		// 1805, 1806
		PreparedStatementRenderer personsByDurationSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/observationPeriodTimeRelativeToIndex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsByDurationSql != null) {
			summary.setPersonsByDurationFromStartToEnd(jdbcTemplate.query(personsByDurationSql.getSql(), personsByDurationSql.getSetter(), new ObservationPeriodMapper()));
		}

		// 1815
		PreparedStatementRenderer monthPrevalenceSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByMonth.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (monthPrevalenceSql != null) {
			summary.setPrevalenceByMonth(jdbcTemplate.query(monthPrevalenceSql.getSql(), monthPrevalenceSql.getSetter(), new PrevalanceMapper()));
		}

		// 1814
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceGenderAgeSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByYearGenderSex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql.getSql(), prevalenceGenderAgeSql.getSetter(), new ConceptDecileCountsMapper());
		}
		summary.setNumPersonsByCohortStartByGenderByAge(prevalenceByGenderAgeYear);

		// 1801
		List<ConceptQuartileRecord> ageAtIndex = null;
		PreparedStatementRenderer ageAtIndexSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/ageAtIndexDistribution.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			ageAtIndex = jdbcTemplate.query(ageAtIndexSql.getSql(), ageAtIndexSql.getSetter(), new ConceptQuartileMapper());
		}
		summary.setAgeAtIndexDistribution(ageAtIndex);

		// 1803
		List<ConceptQuartileRecord> distributionAgeCohortStartByCohortStartYear = null;
		PreparedStatementRenderer distributionAgeCohortStartByCohortStartYearSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByCohortStartYear.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			distributionAgeCohortStartByCohortStartYear = jdbcTemplate.query(distributionAgeCohortStartByCohortStartYearSql.getSql(), distributionAgeCohortStartByCohortStartYearSql.getSetter(), new ConceptQuartileMapper());
		}
		summary.setDistributionAgeCohortStartByCohortStartYear(distributionAgeCohortStartByCohortStartYear);

		// 1802
		List<ConceptQuartileRecord> distributionAgeCohortStartByGender = null;
		PreparedStatementRenderer distributionAgeCohortStartByGenderSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByGender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			distributionAgeCohortStartByGender = jdbcTemplate.query(distributionAgeCohortStartByGenderSql.getSql(), distributionAgeCohortStartByGenderSql.getSetter(), new ConceptQuartileMapper());
		}
		summary.setDistributionAgeCohortStartByGender(distributionAgeCohortStartByGender);

		// 1804
		PreparedStatementRenderer personsInCohortFromCohortStartToEndSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/personsInCohortFromCohortStartToEnd.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsInCohortFromCohortStartToEndSql != null) {
			summary.setPersonsInCohortFromCohortStartToEnd(jdbcTemplate.query(personsInCohortFromCohortStartToEndSql.getSql(), personsInCohortFromCohortStartToEndSql.getSetter(), new MonthObservationMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(summary.getAgeAtIndexDistribution())
				|| CollectionUtils.isNotEmpty(summary.getDistributionAgeCohortStartByCohortStartYear())
				|| CollectionUtils.isNotEmpty(summary.getDistributionAgeCohortStartByGender())
				|| CollectionUtils.isNotEmpty(summary.getNumPersonsByCohortStartByGenderByAge())
				|| CollectionUtils.isNotEmpty(summary.getPersonsByDurationFromStartToEnd())
				|| CollectionUtils.isNotEmpty(summary.getPersonsInCohortFromCohortStartToEnd())
				|| CollectionUtils.isNotEmpty(summary.getPrevalenceByMonth()))  {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, summary);
		}

		return summary;
	}

	public CohortSpecificTreemap getCohortSpecificTreemapResults(JdbcTemplate jdbcTemplate, final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam, Source source,
			boolean save) {

		final String key = COHORT_SPECIFIC_TREEMAP;
		CohortSpecificTreemap summary = new CohortSpecificTreemap();
		boolean empty = true;
		
		// 1820
		PreparedStatementRenderer conditionOccurrencePrevalenceSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/conditionOccurrencePrevalenceOfCondition.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conditionOccurrencePrevalenceSql != null) {
			summary.setConditionOccurrencePrevalence(jdbcTemplate.query(conditionOccurrencePrevalenceSql.getSql(), conditionOccurrencePrevalenceSql.getSetter(), new HierarchicalConceptPrevalenceMapper()));
		}

		// 1830
		PreparedStatementRenderer procedureOccurrencePrevalenceSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/procedureOccurrencePrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (procedureOccurrencePrevalenceSql != null) {
			summary.setProcedureOccurrencePrevalence(jdbcTemplate.query(procedureOccurrencePrevalenceSql.getSql(), procedureOccurrencePrevalenceSql.getSetter(), new HierarchicalConceptPrevalenceMapper()));
		}

		// 1870
		PreparedStatementRenderer drugEraPrevalenceSql = renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/drugEraPrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (drugEraPrevalenceSql != null) {
			summary.setDrugEraPrevalence(jdbcTemplate.query(drugEraPrevalenceSql.getSql(), drugEraPrevalenceSql.getSetter(), new HierarchicalConceptPrevalenceMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(summary.getConditionOccurrencePrevalence())
				|| CollectionUtils.isNotEmpty(summary.getProcedureOccurrencePrevalence())
				|| CollectionUtils.isNotEmpty(summary.getDrugEraPrevalence())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, summary);
		}

		return summary;
	}

	public CohortVisitsDrilldown getCohortVisitsDrilldown(JdbcTemplate jdbcTemplate,
			final int id,
			final int conceptId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		CohortVisitsDrilldown drilldown = new CohortVisitsDrilldown();
		final String key = VISIT_DRILLDOWN;
		boolean empty = true;

		List<ConceptQuartileRecord> ageAtFirst = null;
		PreparedStatementRenderer ageAtFirstSql = prepareDrillDownCohortSql("sqlAgeAtFirstOccurrence", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstOccurrence(ageAtFirst);

		List<ConceptQuartileRecord> byType = null;
		PreparedStatementRenderer byTypeSql = prepareDrillDownCohortSql("sqlVisitDurationByType", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql.getSql(), byTypeSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setVisitDurationByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceGenderAgeSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql.getSql(), prevalenceGenderAgeSql.getSetter(), new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstOccurrence())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getVisitDurationByType())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, drilldown);
		}

		return drilldown;
	}

	public CohortConditionEraDrilldown getConditionEraDrilldown(JdbcTemplate jdbcTemplate,
			final int id,
			final int conditionId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION_ERA_DRILLDOWN;
		CohortConditionEraDrilldown drilldown = new CohortConditionEraDrilldown();
		boolean empty = true;
		
		// age at first diagnosis
		List<ConceptQuartileRecord> ageAtFirst = null;
		PreparedStatementRenderer ageAtFirstSql = prepareDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirst);

		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		PreparedStatementRenderer lengthOfEraSql = prepareDrillDownCohortSql("sqlLengthOfEra", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (lengthOfEraSql != null) {
			lengthOfEra = jdbcTemplate.query(lengthOfEraSql.getSql(), lengthOfEraSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);

		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceByGenderAgeYearSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceByGenderAgeYearSql.getSql(), prevalenceByGenderAgeYearSql.getSetter(), new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalenceByMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalenceByMonthSql.getSql(), prevalenceByMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstDiagnosis())
				|| CollectionUtils.isNotEmpty(drilldown.getLengthOfEra())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conditionId, drilldown);
		}

		return drilldown;
	}

	public List<HierarchicalConceptRecord> getConditionEraTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION_ERA;
		List<HierarchicalConceptRecord> res = null;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/conditionera/sqlConditionEraTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptEraMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortConditionDrilldown getConditionResults(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conditionId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		final String key = CONDITION_DRILLDOWN;
		CohortConditionDrilldown drilldown = new CohortConditionDrilldown();
		boolean empty = true;

		List<ConceptQuartileRecord> ageAtFirstDiagnosis = null;
		PreparedStatementRenderer ageAtFirstSql = prepareDrillDownCohortSql("sqlAgeAtFirstDiagnosis", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirstDiagnosis = jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirstDiagnosis);

		List<ConceptCountRecord> conditionsByType = null;
		PreparedStatementRenderer conditionsSql = prepareDrillDownCohortSql("sqlConditionsByType", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conditionsSql != null) {
			conditionsByType = jdbcTemplate.query(conditionsSql.getSql(), conditionsSql.getSetter(), new ConceptConditionCountMapper());
		}
		drilldown.setConditionsByType(conditionsByType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceGenderAgeSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql.getSql(), prevalenceGenderAgeSql.getSetter(), new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalanceMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql.getSql(), prevalanceMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstDiagnosis())
				|| CollectionUtils.isNotEmpty(drilldown.getConditionsByType())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())) {
			empty = false;
		}
		
		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conditionId, drilldown);
		}
		return drilldown;
	}

	public List<HierarchicalConceptRecord> getConditionTreemap(JdbcTemplate jdbcTemplate, final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION;
		List<HierarchicalConceptRecord> res = null;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/condition/sqlConditionTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortDashboard getDashboard(JdbcTemplate jdbcTemplate, 
			int id, Source source,
			Integer minCovariatePersonCountParam, Integer minIntervalPersonCountParam,
			boolean demographicsOnly,
			boolean save) {
		
		final String key = DASHBOARD;
		CohortDashboard dashboard = new CohortDashboard();
		boolean empty = true;

		PreparedStatementRenderer ageAtFirstObsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstObsSql != null) {
			dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql.getSql(), ageAtFirstObsSql.getSetter(), new ConceptDistributionMapper()));
		}

		PreparedStatementRenderer genderSql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam,
				minIntervalPersonCountParam, source);
		if (genderSql != null) {
			dashboard.setGender(jdbcTemplate.query(genderSql.getSql(), genderSql.getSetter(), new ConceptCountMapper()));
		}

		if (!demographicsOnly) {
			PreparedStatementRenderer cumulObsSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
					minCovariatePersonCountParam, minIntervalPersonCountParam, source);
			if (cumulObsSql != null) {
				dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql.getSql(), cumulObsSql.getSetter(), new CumulativeObservationMapper()));
			}

			PreparedStatementRenderer obsByMonthSql = renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
					minCovariatePersonCountParam, minIntervalPersonCountParam, source);
			if (obsByMonthSql != null) {
				dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql.getSql(), obsByMonthSql.getSetter(), new MonthObservationMapper()));
			}
		}
		
		if (CollectionUtils.isNotEmpty(dashboard.getAgeAtFirstObservation())
				|| CollectionUtils.isNotEmpty(dashboard.getCumulativeObservation())
				|| CollectionUtils.isNotEmpty(dashboard.getGender())
				|| CollectionUtils.isNotEmpty(dashboard.getObservedByMonth())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, dashboard);
		}

		return dashboard;

	}

	public CohortDrugEraDrilldown getDrugEraResults(JdbcTemplate jdbcTemplate,
			final int id,
			final int drugId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		final String key = DRUG_ERA_DRILLDOWN;
		CohortDrugEraDrilldown drilldown = new CohortDrugEraDrilldown();
		boolean empty = true;

		// age at first exposure
		List<ConceptQuartileRecord> ageAtFirstExposure = null;
		PreparedStatementRenderer ageAtFirstExposureSql = prepareDrillDownCohortSql("sqlAgeAtFirstExposure", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstExposureSql != null) {
			ageAtFirstExposure = jdbcTemplate.query(ageAtFirstExposureSql.getSql(), ageAtFirstExposureSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstExposure(ageAtFirstExposure);

		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		PreparedStatementRenderer lengthOfEraSql = prepareDrillDownCohortSql("sqlLengthOfEra", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (lengthOfEraSql != null) {
			lengthOfEra = jdbcTemplate.query(lengthOfEraSql.getSql(), lengthOfEraSql.getSetter(), new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);

		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		PreparedStatementRenderer prevalenceByGenderAgeYearSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceByGenderAgeYearSql.getSql(), prevalenceByGenderAgeYearSql.getSetter(), new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		PreparedStatementRenderer prevalenceByMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalenceByMonthSql.getSql(), prevalenceByMonthSql.getSetter(), new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstExposure())
				|| CollectionUtils.isNotEmpty(drilldown.getLengthOfEra())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())) {
			empty = false;
		}
		
		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, drugId, drilldown);
		}

		return drilldown;

	}

	public List<HierarchicalConceptRecord> getDrugEraTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = DRUG_ERA;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/drugera/sqlDrugEraTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptEraMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortDrugDrilldown getDrugResults(JdbcTemplate jdbcTemplate, 
			final int id, 
			final int drugId,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		final String key = DRUG_DRILLDOWN;
		CohortDrugDrilldown drilldown = new CohortDrugDrilldown();
		boolean empty = true;

		PreparedStatementRenderer ageAtFirstExposureSql = prepareDrillDownCohortSql("sqlAgeAtFirstExposure", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstExposureSql != null) {
			drilldown.setAgeAtFirstExposure(jdbcTemplate.query(ageAtFirstExposureSql.getSql(), ageAtFirstExposureSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer daysSupplySql = prepareDrillDownCohortSql("sqlDaysSupplyDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (daysSupplySql != null) {
			drilldown.setDaysSupplyDistribution(jdbcTemplate.query(daysSupplySql.getSql(), daysSupplySql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer drugsByTypeSql = prepareDrillDownCohortSql("sqlDrugsByType", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (drugsByTypeSql != null) {
			drilldown.setDrugsByType(jdbcTemplate.query(drugsByTypeSql.getSql(), drugsByTypeSql.getSetter(), new ConceptCountMapper()));
		}

		PreparedStatementRenderer prevalenceByGenderAgeYearSql = prepareDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(prevalenceByGenderAgeYearSql.getSql(), prevalenceByGenderAgeYearSql.getSetter(),
					new ConceptDecileMapper()));
		}

		PreparedStatementRenderer prevalenceByMonthSql = prepareDrillDownCohortSql("sqlPrevalenceByMonth", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			drilldown.setPrevalenceByMonth(jdbcTemplate.query(prevalenceByMonthSql.getSql(), prevalenceByMonthSql.getSetter(), new PrevalanceConceptMapper()));
		}

		PreparedStatementRenderer quantityDistributionSql = prepareDrillDownCohortSql("sqlQuantityDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (quantityDistributionSql != null) {
			drilldown.setQuantityDistribution(jdbcTemplate.query(quantityDistributionSql.getSql(), quantityDistributionSql.getSetter(), new ConceptQuartileMapper()));
		}

		PreparedStatementRenderer refillsDistributionSql = prepareDrillDownCohortSql("sqlRefillsDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (refillsDistributionSql != null) {
			drilldown.setRefillsDistribution(jdbcTemplate.query(refillsDistributionSql.getSql(), refillsDistributionSql.getSetter(), new ConceptQuartileMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstExposure())
				|| CollectionUtils.isNotEmpty(drilldown.getDaysSupplyDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getDrugsByType())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getQuantityDistribution())
				|| CollectionUtils.isNotEmpty(drilldown.getRefillsDistribution())) {
			empty = false;
		}

		if (!empty && save) {
			saveEntityDrilldown(id, source.getSourceId(), key, drugId, drilldown);
		}

		return drilldown;

	}

	public List<HierarchicalConceptRecord> getDrugTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source, boolean save) {


		final String key = DRUG;
		List<HierarchicalConceptRecord> res = null;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/drug/sqlDrugTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}


		return res;
	}

	public List<CohortAttribute> getHeraclesHeel(JdbcTemplate jdbcTemplate,
			final int id, 
			Source source,
			boolean save) {
		List<CohortAttribute> attrs = new ArrayList<>();
		final String key = HERACLES_HEEL;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/heraclesHeel/sqlHeraclesHeel.sql", id, null, null, source);
		if (sql != null) {
			attrs = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new CohortAttributeMapper());
		}
		if (save) {
			saveEntity(id, source.getSourceId(), key, attrs);
		}

		return attrs;
	}

	/**
	 * Queries for cohort analysis person results for the given cohort definition
	 * id
	 *
	 * @param id cohort_defintion id
	 * @return CohortPersonSummary
	 */
	public CohortPersonSummary getPersonResults(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			final Source source,
			boolean save) {
		
		final String key = PERSON;
		CohortPersonSummary person = new CohortPersonSummary();
		boolean empty = true;

		PreparedStatementRenderer yobSql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_data.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (yobSql != null) {
			person.setYearOfBirth(jdbcTemplate.query(yobSql.getSql(), yobSql.getSetter(), new ConceptDistributionMapper()));
		}

		PreparedStatementRenderer yobStatSql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (yobStatSql != null) {
			person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql.getSql(), yobStatSql.getSetter(), new CohortStatsMapper()));
		}

		PreparedStatementRenderer genderSql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (genderSql != null) {
			person.setGender(jdbcTemplate.query(genderSql.getSql(), genderSql.getSetter(), new ConceptCountMapper()));
		}

		PreparedStatementRenderer raceSql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (raceSql != null) {
			person.setRace(jdbcTemplate.query(raceSql.getSql(), raceSql.getSetter(), new ConceptCountMapper()));
		}

		PreparedStatementRenderer ethnicitySql = renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ethnicitySql != null) {
			person.setEthnicity(jdbcTemplate.query(ethnicitySql.getSql(), ethnicitySql.getSetter(), new ConceptCountMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(person.getEthnicity())
				|| CollectionUtils.isNotEmpty(person.getGender())
				|| CollectionUtils.isNotEmpty(person.getRace())
				|| CollectionUtils.isNotEmpty(person.getYearOfBirth())
				|| CollectionUtils.isNotEmpty(person.getYearOfBirthStats())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, person);
		}


		return person;
	}

	public List<HierarchicalConceptRecord> getProcedureTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = PROCEDURE;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/procedure/sqlProcedureTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public List<HierarchicalConceptRecord> getVisitTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = VISIT;

		PreparedStatementRenderer sql = renderTranslateCohortSql(BASE_SQL_PATH + "/visit/sqlVisitTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new HierarchicalConceptMapper());
			if (save) {
				saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	protected PreparedStatementRenderer prepareDrillDownCohortSql(
			String analysisName, String analysisType, Integer id, Integer conceptId,
			final Integer minCovariatePersonCountParam, final Integer minIntervalPersonCountParam, Source source) {

		String sqlPath = BASE_SQL_PATH + "/" + analysisType + "/byConcept/" + analysisName + ".sql";
		return prepareCohortSql(sqlPath, id, conceptId, minCovariatePersonCountParam,
			minIntervalPersonCountParam, source);

	}

	protected PreparedStatementRenderer prepareCohortSql(
			String sqlPath, Integer id,Integer conceptId, final Integer minCovariatePersonCountParam,
			final Integer minIntervalPersonCountParam, Source source
	) {

		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

		String[] search = new String[]{"cdm_database_schema", "ohdsi_database_schema"};
		String[] replace = new String[]{vocabularyTableQualifier, resultsTableQualifier};

		String[] cols;
		Object[] colValues;
		if (conceptId != null) {
			cols = new String[]{"cohortDefinitionId", "minCovariatePersonCount", "minIntervalPersonCount", "conceptId"};

			colValues = new Integer[]{id,
					minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
							: minCovariatePersonCountParam,
					minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
							: minIntervalPersonCountParam,
					conceptId};
		} else {
			cols = new String[]{"cohortDefinitionId", "minCovariatePersonCount", "minIntervalPersonCount"};

			colValues = new Integer[]{id,
					minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
							: minCovariatePersonCountParam,
					minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
							: minIntervalPersonCountParam};
		}

		return new PreparedStatementRenderer(source, sqlPath, search, replace, cols, colValues);
	}

	/**
	 * Passes in common params for cohort results, and performs SQL
	 * translate/render
	 */
	public PreparedStatementRenderer renderTranslateCohortSql(String sqlPath, Integer id,
			final Integer minCovariatePersonCountParam, final Integer minIntervalPersonCountParam, Source source) {
		return prepareCohortSql(sqlPath, id, null, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
	}

	private void saveEntity(int cohortDefinitionId, int sourceId, String visualizationKey, Object dataObject) {
		if (dataObject == null) {
			log.error("Cannot store null entity {}", visualizationKey);
			return;
		}
		
		if (dataObject instanceof List) {
			List<?> listObject = (List<?>) dataObject;
			if (listObject.size() == 0) {
				log.warn("No need to store empty list for {}", visualizationKey);
				return;
			}
		}

		// delete the old one
		try {
			this.visualizationDataRepository
				.deleteByCohortDefinitionIdAndSourceIdAndVisualizationKey(cohortDefinitionId, sourceId, visualizationKey);
			
			// delete any 'drilldown'
			List<VisualizationData> drilldownData = this.visualizationDataRepository
					.findDistinctVisualizationDataByCohortDefinitionIdAndSourceIdAndVisualizationKey(cohortDefinitionId, sourceId, visualizationKey + "_drilldown");
			if (CollectionUtils.isNotEmpty(drilldownData)) {
				for (VisualizationData drilldownItem : drilldownData) {
					this.visualizationDataRepository.delete(drilldownItem);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		// save entity
		try {
			
			VisualizationData entity = new VisualizationData();
			entity.setCohortDefinitionId(cohortDefinitionId);
			entity.setSourceId(sourceId);
			entity.setVisualizationKey(visualizationKey);
			entity.setEndTime(new Date());

			String dataString = mapper.writeValueAsString(dataObject);
			entity.setData(dataString);

			this.visualizationDataRepository.save(entity);
		} catch (Exception e) {
			log.error(whitelist(e));
		}
	}

	private void saveEntityDrilldown(int cohortDefinitionId, int sourceId, String visualizationKey, int drilldownId, Object dataObject) {
		if (dataObject == null) {
			log.error("Cannot store null entity {}", visualizationKey);
			return;
		}
		
		if (dataObject instanceof List) {
			List<?> listObject = (List<?>) dataObject;
			if (listObject.size() == 0) {
				log.warn("No need to store empty list for {}",  visualizationKey);
				return;
			}
		}
		
		// delete the old one
		try {
			this.visualizationDataRepository
				.deleteByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(cohortDefinitionId, sourceId, visualizationKey, drilldownId);
		} catch (Exception e) {
			log.error(whitelist(e));
		}
		
		// save entity
		try {
			VisualizationData entity = new VisualizationData();
			entity.setCohortDefinitionId(cohortDefinitionId);
			entity.setSourceId(sourceId);
			entity.setVisualizationKey(visualizationKey);
			entity.setDrilldownId(drilldownId);
			entity.setEndTime(new Date());

			String dataString = mapper.writeValueAsString(dataObject);
			entity.setData(dataString);

			this.visualizationDataRepository.save(entity);
		} catch (Exception e) {
			log.error(whitelist(e));
		}
	}

	public int warmupData(JdbcTemplate jdbcTemplate, CohortAnalysisTask task) {
		int count = 0;
		if (task != null && task.getCohortDefinitionIds() != null && task.getVisualizations() != null) {
			for (String id : task.getCohortDefinitionIds()) {
				for (String viz : task.getVisualizations()) {
					if (DEFAULT.equals(viz) || DASHBOARD.equals(viz)) {
						getDashboard(jdbcTemplate, Integer.valueOf(id), task.getSource(), null, null, false, true);
						count++;
					} else if (COHORT_SPECIFIC.equals(viz)) {
						getCohortSpecificSummary(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
						// treemaps are separate
						getCohortSpecificTreemapResults(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (CONDITION.equals(viz)) {
						getConditionTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (CONDITION_ERA.equals(viz)) {
						getConditionEraTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (DRUG.equals(viz) || DRUG_EXPOSURE.equals(viz)) {
						getDrugTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (DRUG_ERA.equals(viz)) {
						getDrugEraTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (PERSON.equals(viz)) {
						getPersonResults(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (OBSERVATION.equals(viz)) {
						getCohortObservationResults(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (MEASUREMENT.equals(viz)) {
						getCohortMeasurementResults(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (OBSERVATION_PERIOD.equals(viz) || OBSERVATION_PERIODS.equals(viz)) {
						getCohortObservationPeriod(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (DATA_DENSITY.equals(viz)) {
						getCohortDataDensity(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (PROCEDURE.equals(viz)) {
						getProcedureTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (VISIT.equals(viz)) {
						getVisitTreemap(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (DEATH.equals(viz)) {
						getCohortDeathData(jdbcTemplate, Integer.valueOf(id), null, null, task.getSource(), true);
						count++;
					} else if (HERACLES_HEEL.equals(viz)) {
						getHeraclesHeel(jdbcTemplate, Integer.valueOf(id), task.getSource(), true);
						count++;
					}
				}
			}
		}

		return count;
	}
	
	/* Healthcare Utilizaton Reports */
	
	public HealthcareExposureReport getHealthcareExposureReport(JdbcTemplate jdbcTemplate, final int cohortId, final WindowType window, final PeriodType periodType, Source source) {
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		int windowAnalysisId = getSubjectAnalysisIdByWindowType(window);

        String[] search = new String[]{"results_schema"};
		String[] replace = new String[]{resultsTableQualifier};

		HealthcareExposureReport report = new HealthcareExposureReport();
		
		String summaryPath = BASE_SQL_PATH + "/healthcareutilization/getExposureSummary.sql";
		String[] summaryCols = new String[]{"cohort_definition_id","analysis_id"};
		Object[] summaryColVals = new Object[]{cohortId, windowAnalysisId};

		PreparedStatementRenderer summaryPsr =  new PreparedStatementRenderer(source, summaryPath, search, replace, summaryCols, summaryColVals);
		
		report.summary = jdbcTemplate.query(summaryPsr.getSql(), summaryPsr.getSetter(), (rs,rowNum) -> {
			HealthcareExposureReport.Summary s = new HealthcareExposureReport.Summary();
			s.personsCount = rs.getLong("person_total");
			s.exposureTotal = new BigDecimal(rs.getDouble("exposure_years_total")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.exposureAvg = new BigDecimal(rs.getDouble("exposure_avg_years_1k")).setScale(2, BigDecimal.ROUND_HALF_UP);
			return s;
		}).get(0);
		
		String dataPath = BASE_SQL_PATH + "/healthcareutilization/getExposureData.sql";
		String[] dataCols = new String[]{"cohort_definition_id","analysis_id", "period_type"};
		Object[] dataColVals = new Object[]{cohortId, windowAnalysisId, periodType.toString().toLowerCase()};

		PreparedStatementRenderer dataPsr =  new PreparedStatementRenderer(source, dataPath, search, replace, dataCols, dataColVals);
		
		report.data = jdbcTemplate.query(dataPsr.getSql(), dataPsr.getSetter(), (rs,rowNum) -> {
			HealthcareExposureReport.ReportItem item = new HealthcareExposureReport.ReportItem();
			item.periodType = rs.getString("period_type");
			item.periodStart = rs.getDate("period_start_date");
			item.periodEnd = rs.getDate("period_end_date");
			item.personsCount = rs.getLong("person_total");
			item.personsPct = new BigDecimal(rs.getDouble("person_percent")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.exposureTotal = new BigDecimal(rs.getDouble("exposure_years_total")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.exposurePct = new BigDecimal(rs.getDouble("exposure_percent")).setScale(2, BigDecimal.ROUND_HALF_UP); 
			item.exposureAvg = new BigDecimal(rs.getDouble("exposure_avg_years_1k")).setScale(2, BigDecimal.ROUND_HALF_UP);
			return item;
		});

		
		return report;
	}

    private int getSubjectAnalysisIdByWindowType(final WindowType window) {
        if (window == WindowType.BASELINE) {
            return 4000;
        } else if (window == WindowType.AT_RISK) {
            return 4006;
        } else {
            throw new RuntimeException("Invalid window type: " + window);
        }
    }

    public HealthcareVisitUtilizationReport getHealthcareVisitReport(JdbcTemplate jdbcTemplate, final int cohortId, final WindowType window, VisitStatType visitStat
		, final PeriodType periodType, final Long visitConceptId, final Long visitTypeConceptId, final Long costTypeConceptId
		, Source source) {
		String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
		if (vocabularyTableQualifier == null) {
			vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM);
		};
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		int subjectsAnalysisId;
		int subjectWithRecordsAnalysisId;		
		int visitStatAnalysisId;
		int losAnalysisId;
		int costAnalysisId;
		
		// set apprpriate analysis IDs
		if (window == WindowType.BASELINE) {
			subjectsAnalysisId = 4000;
			subjectWithRecordsAnalysisId = 4001;
			losAnalysisId = 4005;
			costAnalysisId = 4020;
			switch(visitStat) {
				case OCCURRENCE:
					visitStatAnalysisId = 4002;
					break;
				case VISIT_DATE:
					visitStatAnalysisId = 4003;
					break;
				case CARESITE_DATE:
					visitStatAnalysisId = 4004;
					break;
				default:
					throw new RuntimeException("Invalid visitStat: " + visitStat);
			}
		} else if (window == WindowType.AT_RISK) {
			subjectsAnalysisId = 4006;
			subjectWithRecordsAnalysisId = 4007;
			losAnalysisId = 4011;			
			costAnalysisId = 4021;
			switch(visitStat) {
				case OCCURRENCE:
					visitStatAnalysisId = 4008;
					break;
				case VISIT_DATE:
					visitStatAnalysisId = 4009;
					break;
				case CARESITE_DATE:
					visitStatAnalysisId = 4010;
					break;
				default:
					throw new RuntimeException("Invalid visitStat: " + visitStat);
			}
		} else {
			throw new RuntimeException("Invalid window type: " + window);			
		}
		
		String[] search = new String[]{"vocabulary_schema","results_schema"};
		String[] replace = new String[]{vocabularyTableQualifier, resultsTableQualifier};

		HealthcareVisitUtilizationReport report = new HealthcareVisitUtilizationReport();
		
		String reportPath = BASE_SQL_PATH + "/healthcareutilization/getVisitUtilization.sql";
		String reportSql = ResourceHelper.GetResourceAsString(reportPath);
		
		String visitConceptIdStr = visitConceptId == null ? "" : visitConceptId.toString();
		String visitTypeConceptIdStr = visitTypeConceptId == null ? "" : visitTypeConceptId.toString();
		
		String summarySql = SqlRender.renderSql(reportSql, new String[] {"is_summary", "visit_concept_id", "visit_type_concept_id"}, 
				new String[]{"TRUE", visitConceptIdStr, visitTypeConceptIdStr});
		
		String[] reportCols = new String[]{"cohort_definition_id"
			, "subjects_analysis_id"
			, "subject_with_records_analysis_id"
			, "visit_stat_analysis_id"
			, "los_analysis_id"
			, "cost_analysis_id"
			, "visit_concept_id"
			, "visit_type_concept_id"
			, "cost_type_concept_id"
			, "period_type"
		};
		Object[] colVals = new Object[]{cohortId
			, subjectsAnalysisId
			, subjectWithRecordsAnalysisId
			, visitStatAnalysisId
			, losAnalysisId
			, costAnalysisId
			, visitConceptIdStr
			, visitTypeConceptIdStr
			, costTypeConceptId == null ? "" : costTypeConceptId.toString()
			, periodType.toString().toLowerCase()
		};

		PreparedStatementRenderer summaryPsr =  new PreparedStatementRenderer(source, summarySql, search, replace, reportCols, colVals);
		List<HealthcareVisitUtilizationReport.Summary> summaryRows = jdbcTemplate.query(summaryPsr.getSql(), summaryPsr.getSetter(), (rs,rowNum) -> {
			HealthcareVisitUtilizationReport.Summary s = new HealthcareVisitUtilizationReport.Summary();
			s.personsCount = rs.getLong("person_total");
			s.personsPct = new BigDecimal(rs.getDouble("person_percent")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.visitsCount = rs.getLong("records_total");
			s.visitsPer1000 = new BigDecimal(rs.getDouble("records_per_1000")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.visitsPer1000WithVisits = new BigDecimal(rs.getDouble("records_per_1000_with_record")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.visitsPer1000PerYear = new BigDecimal(rs.getDouble("records_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.lengthOfStayTotal = rs.getLong("los_total");
			s.lengthOfStayAvg = rs.getString("los_average") == null ? null : new BigDecimal(rs.getDouble("los_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.allowed = rs.getString("allowed") == null ? null : new BigDecimal(rs.getDouble("allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.allowedPmPm = rs.getString("allowed_pmpm") == null ? null : new BigDecimal(rs.getDouble("allowed_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.charged = rs.getString("charged") == null ? null : new BigDecimal(rs.getDouble("charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.chargedPmPm = rs.getString("charged_pmpm") == null ? null : new BigDecimal(rs.getDouble("charged_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.paid = rs.getString("paid") == null ? null : new BigDecimal(rs.getDouble("paid")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.paidPmPm = rs.getString("paid_pmpm") == null ? null : new BigDecimal(rs.getDouble("paid_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.allowedChargedRatio = rs.getString("allowed_charged") == null ? null : new BigDecimal(rs.getDouble("allowed_charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			s.paidAllowedRatio = rs.getString("paid_allowed") == null ? null : new BigDecimal(rs.getDouble("paid_allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);

			return s;
		});
		
		report.summary = summaryRows.size() > 0 ? summaryRows.get(0) : new HealthcareVisitUtilizationReport.Summary();

		String dataSql = SqlRender.renderSql(reportSql, new String[] {"is_summary"}, new String[]{"FALSE"});
		
		PreparedStatementRenderer dataPsr =  new PreparedStatementRenderer(source, dataSql, search, replace, reportCols, colVals);
		
		report.data = jdbcTemplate.query(dataPsr.getSql(), dataPsr.getSetter(), (rs,rowNum) -> {
			HealthcareVisitUtilizationReport.ReportItem item = new HealthcareVisitUtilizationReport.ReportItem();
			item.periodType = rs.getString("period_type");
			item.periodStart = rs.getDate("period_start_date");
			item.periodEnd = rs.getDate("period_end_date");
			item.personsCount = rs.getLong("person_total");
			item.personsPct = new BigDecimal(rs.getDouble("person_percent")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.visitsCount = rs.getLong("records_total");
			item.visitsPer1000 = new BigDecimal(rs.getDouble("records_per_1000")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.visitsPer1000WithVisits = new BigDecimal(rs.getDouble("records_per_1000_with_record")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.visitsPer1000PerYear = new BigDecimal(rs.getDouble("records_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.lengthOfStayTotal = rs.getLong("los_total");
			item.lengthOfStayAvg = new BigDecimal(rs.getDouble("los_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.allowed = rs.getString("allowed") == null ? null : new BigDecimal(rs.getDouble("allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.allowedPmPm = rs.getString("allowed_pmpm") == null ? null : new BigDecimal(rs.getDouble("allowed_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.charged = rs.getString("charged") == null ? null : new BigDecimal(rs.getDouble("charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.chargedPmPm = rs.getString("charged_pmpm") == null ? null : new BigDecimal(rs.getDouble("charged_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.paid = rs.getString("paid") == null ? null : new BigDecimal(rs.getDouble("paid")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.paidPmPm = rs.getString("paid_pmpm") == null ? null : new BigDecimal(rs.getDouble("paid_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.allowedChargedRatio = rs.getString("allowed_charged") == null ? null : new BigDecimal(rs.getDouble("allowed_charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			item.paidAllowedRatio = rs.getString("paid_allowed") == null ? null : new BigDecimal(rs.getDouble("paid_allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			return item;
		});

		// load visit and visit type concepts
		String[] conceptCols = new String[]{"cohort_definition_id", "analysis_id"};
		Object[] conceptColValues = new Object[]{cohortId, visitStatAnalysisId};
		
		String visitConceptQuery = BASE_SQL_PATH + "/healthcareutilization/getVisitConceptsForAnalysis.sql";
		PreparedStatementRenderer visitConceptPsr =  new PreparedStatementRenderer(source, visitConceptQuery, search, replace, conceptCols, conceptColValues);
		report.visitConcepts = jdbcTemplate.query(visitConceptPsr.getSql(), visitConceptPsr.getSetter(), (rs,rowNum) -> {
			Concept c = new Concept();
			c.conceptName = rs.getString("concept_name");
			c.conceptId = rs.getLong("concept_id");
			return c;
		});
		
		String visitTypeConceptQuery = BASE_SQL_PATH + "/healthcareutilization/getVisitTypeConceptsForAnalysis.sql";
		PreparedStatementRenderer visitTypeConceptPsr =  new PreparedStatementRenderer(source, visitTypeConceptQuery, search, replace, conceptCols, conceptColValues);
		report.visitTypeConcepts = jdbcTemplate.query(visitTypeConceptPsr.getSql(), visitTypeConceptPsr.getSetter(), (rs,rowNum) -> {
			Concept c = new Concept();
			c.conceptName = rs.getString("concept_name");
			c.conceptId = rs.getLong("concept_id");
			return c;
		});
		
		return report;
	}
	
	public HealthcareDrugUtilizationSummary getHealthcareDrugUtilizationSummary(JdbcTemplate jdbcTemplate, final int cohortId, final WindowType window, Long drugTypeConceptId, Long costTypeConceptId, Source source) {
		String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
		if (vocabularyTableQualifier == null) {
			vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM);
		}
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		int subjectsAnalysisId;
		int subjectWithRecordsAnalysisId;
		int drugAnalysisId;
		int daysSupplyAnalysisId;
		int quantityAnalysisId;
		int costAnalysisId;
		
		// set apprpriate analysis IDs
		if (window == WindowType.BASELINE) {
			subjectsAnalysisId = 4000;
			subjectWithRecordsAnalysisId = 4012;
			drugAnalysisId = 4013;
			daysSupplyAnalysisId = 4014;
			quantityAnalysisId = 4015;
			costAnalysisId = 4022;			
		} else if (window == WindowType.AT_RISK) {
			subjectsAnalysisId = 4006;
			subjectWithRecordsAnalysisId = 4016;
			drugAnalysisId = 4017;
			daysSupplyAnalysisId = 4018;
			quantityAnalysisId = 4019;
			costAnalysisId = 4023;
		} else {
			throw new RuntimeException("Invalid window type: " + window);			
		}
		
		String[] search = new String[]{"vocabulary_schema","results_schema"};
		String[] replace = new String[]{vocabularyTableQualifier, resultsTableQualifier};

		String reportPath = BASE_SQL_PATH + "/healthcareutilization/getDrugUtilization.sql";
		String reportSql = ResourceHelper.GetResourceAsString(reportPath);
		
		String summarySql = SqlRender.renderSql(reportSql, new String[] {"is_summary"}, new String[]{"TRUE"});
		
		String[] reportCols = new String[]{"cohort_definition_id"
			, "subjects_analysis_id"
			, "subject_with_records_analysis_id"
			, "drug_analysis_id"
			, "days_supply_analysis_id"
			, "quantity_analysis_id"
			, "cost_analysis_id"
			, "drug_type_concept_id"
			, "cost_type_concept_id"
			, "period_type"
		};
		Object[] summaryColVals = new Object[]{cohortId
			, subjectsAnalysisId
			, subjectWithRecordsAnalysisId
			, drugAnalysisId
			, daysSupplyAnalysisId
			, quantityAnalysisId
			, costAnalysisId
			, drugTypeConceptId == null ? ""  : drugTypeConceptId.toString()
			, costTypeConceptId == null ? "" : costTypeConceptId.toString()
			, ""
		};

		PreparedStatementRenderer summaryPsr =  new PreparedStatementRenderer(source, summarySql, search, replace, reportCols, summaryColVals);
		HealthcareDrugUtilizationSummary summary = new HealthcareDrugUtilizationSummary();
		
		summary.data = jdbcTemplate.query(summaryPsr.getSql(), summaryPsr.getSetter(), (rs,rowNum) -> {
			HealthcareDrugUtilizationSummary.Record r = new HealthcareDrugUtilizationSummary.Record();
			r.drugName = rs.getString("drug_concept_name");
			r.drugId = rs.getString("stratum_2");
			r.drugCode = rs.getString("drug_concept_code");
			r.drugClass = rs.getString("drug_concept_class");
			r.drugVocabularyId = rs.getString("drug_vocabulary_id");
			r.personsCount = rs.getLong("person_total");
			r.personsPct = new BigDecimal(rs.getDouble("person_percent")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposureCount = rs.getLong("records_total");
			r.exposuresPer1000= new BigDecimal(rs.getDouble("records_per_1000")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposurePer1000WithExposures = new BigDecimal(rs.getDouble("records_per_1000_with_record")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposurePer1000PerYear = new BigDecimal(rs.getDouble("records_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.quantityTotal = rs.getLong("quantity_total");
			r.quantityAvg = new BigDecimal(rs.getDouble("quantity_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.quantityPer1000PerYear = new BigDecimal(rs.getDouble("quantity_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.daysSupplyTotal = rs.getLong("days_supply_total");
			r.daysSupplyAvg = new BigDecimal(rs.getDouble("days_supply_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.daysSupplyPer1000PerYear = new BigDecimal(rs.getDouble("days_supply_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowed = rs.getString("allowed") == null ? null : new BigDecimal(rs.getDouble("allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowedPmPm = rs.getString("allowed_pmpm") == null ? null : new BigDecimal(rs.getDouble("allowed_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.charged = rs.getString("charged") == null ? null : new BigDecimal(rs.getDouble("charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.chargedPmPm = rs.getString("charged_pmpm") == null ? null : new BigDecimal(rs.getDouble("charged_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paid = rs.getString("paid") == null ? null : new BigDecimal(rs.getDouble("paid")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paidPmPm = rs.getString("paid_pmpm") == null ? null : new BigDecimal(rs.getDouble("paid_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowedChargedRatio = rs.getString("allowed_charged") == null ? null : new BigDecimal(rs.getDouble("allowed_charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paidAllowedRatio = rs.getString("paid_allowed") == null ? null : new BigDecimal(rs.getDouble("paid_allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			return r;
		});
		return summary;
	}	

	public HealthcareDrugUtilizationDetail getHealthcareDrugUtilizationReport(JdbcTemplate jdbcTemplate, final int cohortId, final WindowType window, 
		Long drugConceptId, Long drugTypeConceptId, PeriodType periodType, Long costTypeConceptId, Source source) {
		String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
		if (vocabularyTableQualifier == null) {
			vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM);
		}
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		int subjectsAnalysisId;
		int subjectWithRecordsAnalysisId;
		int drugAnalysisId;
		int daysSupplyAnalysisId;
		int quantityAnalysisId;
		int costAnalysisId;
		
		// set apprpriate analysis IDs
		if (window == WindowType.BASELINE) {
			subjectsAnalysisId = 4000;
			subjectWithRecordsAnalysisId = 4012;
			drugAnalysisId = 4013;
			daysSupplyAnalysisId = 4014;
			quantityAnalysisId = 4015;
			costAnalysisId = 4022;
		} else if (window == WindowType.AT_RISK) {
			subjectsAnalysisId = 4006;
			subjectWithRecordsAnalysisId = 4016;
			drugAnalysisId = 4017;
			daysSupplyAnalysisId = 4018;
			quantityAnalysisId = 4019;
			costAnalysisId = 4023;
		} else {
			throw new RuntimeException("Invalid window type: " + window);			
		}
		
		String[] search = new String[]{"vocabulary_schema","results_schema"};
		String[] replace = new String[]{vocabularyTableQualifier, resultsTableQualifier};	
		
		String reportPath = BASE_SQL_PATH + "/healthcareutilization/getDrugUtilization.sql";
		String reportSql = ResourceHelper.GetResourceAsString(reportPath);
		
		String detailSql = SqlRender.renderSql(reportSql, new String[] {"is_summary"}, new String[]{"FALSE"});
		
		String[] reportCols = new String[]{"cohort_definition_id"
			, "subjects_analysis_id"
			, "subject_with_records_analysis_id"
			, "drug_analysis_id"
			, "days_supply_analysis_id"
			, "quantity_analysis_id"
			, "cost_analysis_id"
			, "drug_concept_id"
			, "drug_type_concept_id"
			, "cost_type_concept_id"
			, "period_type"
		};
		Object[] reportColVals = new Object[]{cohortId
			, subjectsAnalysisId
			, subjectWithRecordsAnalysisId
			, drugAnalysisId
			, daysSupplyAnalysisId
			, quantityAnalysisId
			, costAnalysisId			
			, drugConceptId.toString()
			, drugTypeConceptId == null ? "" : drugTypeConceptId.toString()
			, costTypeConceptId == null ? "" : costTypeConceptId.toString()
			, periodType.toString().toLowerCase()
		};

		PreparedStatementRenderer detailPsr =  new PreparedStatementRenderer(source, detailSql, search, replace, reportCols, reportColVals);
		HealthcareDrugUtilizationDetail detail = new HealthcareDrugUtilizationDetail();

		detail.data = jdbcTemplate.query(detailPsr.getSql(), detailPsr.getSetter(), (rs,rowNum) -> {
			HealthcareDrugUtilizationDetail.Record r = new HealthcareDrugUtilizationDetail.Record();
			r.periodType = rs.getString("period_type");
			r.periodStart = rs.getDate("period_start_date");
			r.periodEnd = rs.getDate("period_end_date");
			r.personsCount = rs.getLong("person_total");
			r.personsPct = new BigDecimal(rs.getDouble("person_percent")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposureCount = rs.getLong("records_total");
			r.exposuresPer1000= new BigDecimal(rs.getDouble("records_per_1000")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposurePer1000WithExposures = new BigDecimal(rs.getDouble("records_per_1000_with_record")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.exposurePer1000PerYear = new BigDecimal(rs.getDouble("records_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.quantityTotal = rs.getLong("quantity_total");
			r.quantityAvg = new BigDecimal(rs.getDouble("quantity_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.quantityPer1000PerYear = new BigDecimal(rs.getDouble("quantity_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.daysSupplyTotal = rs.getLong("days_supply_total");
			r.daysSupplyAvg = new BigDecimal(rs.getDouble("days_supply_average")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.daysSupplyPer1000PerYear = new BigDecimal(rs.getDouble("days_supply_per_1000_per_year")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowed = rs.getString("allowed") == null ? null : new BigDecimal(rs.getDouble("allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowedPmPm = rs.getString("allowed_pmpm") == null ? null : new BigDecimal(rs.getDouble("allowed_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.charged = rs.getString("charged") == null ? null : new BigDecimal(rs.getDouble("charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.chargedPmPm = rs.getString("charged_pmpm") == null ? null : new BigDecimal(rs.getDouble("charged_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paid = rs.getString("paid") == null ? null : new BigDecimal(rs.getDouble("paid")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paidPmPm = rs.getString("paid_pmpm") == null ? null : new BigDecimal(rs.getDouble("paid_pmpm")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.allowedChargedRatio = rs.getString("allowed_charged") == null ? null : new BigDecimal(rs.getDouble("allowed_charged")).setScale(2, BigDecimal.ROUND_HALF_UP);
			r.paidAllowedRatio = rs.getString("paid_allowed") == null ? null : new BigDecimal(rs.getDouble("paid_allowed")).setScale(2, BigDecimal.ROUND_HALF_UP);
			return r;
		});
		
		return detail;
	}
	
	public List<Concept> getDrugTypes(JdbcTemplate jdbcTemplate, final int cohortId, Long drugConceptId, Source source) {
		String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
		if (vocabularyTableQualifier == null) {
			vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM);
		}
		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

		String analysisList = StringUtils.join(new Integer[]{4012, 4016}, ",");
		String[] search = new String[]{"vocabulary_schema","results_schema", "analysis_id_list"};
		String[] replace = new String[]{vocabularyTableQualifier, resultsTableQualifier, analysisList};	
		
		String queryPath = BASE_SQL_PATH + "/healthcareutilization/getDrugTypes.sql";
		String[] reportCols = new String[]{"cohort_definition_id", "drug_concept_id"};
		Object[] reportColVals = new Object[]{cohortId, drugConceptId};

		PreparedStatementRenderer detailPsr =  new PreparedStatementRenderer(source, queryPath, search, replace, reportCols, reportColVals);

		List<Concept> drugTypes = jdbcTemplate.query(detailPsr.getSql(), detailPsr.getSetter(), (rs,rowNum) -> {
			Concept c = new Concept();
			c.conceptId = Long.parseLong(rs.getString("concept_id"));
			c.conceptName = rs.getString("concept_name");
			return c;
		});
		
		return drugTypes;
	}

	public List<String> getHealthcarePeriodTypes(final JdbcTemplate jdbcTemplate, final int id, final WindowType window, final Source source) {
        final int windowAnalysisId = getSubjectAnalysisIdByWindowType(window);
        final String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
 
        final String[] search = new String[]{"results_schema"};
        final String[] replace = new String[]{resultsTableQualifier};
 
        final String dataPath = BASE_SQL_PATH + "/healthcareutilization/getPeriodTypes.sql";
        final String[] dataCols = new String[]{"cohort_definition_id","analysis_id"};
        final Object[] dataColVals = new Object[]{id, windowAnalysisId};
 
        final PreparedStatementRenderer dataPsr =  new PreparedStatementRenderer(source, dataPath, search, replace, dataCols, dataColVals);
        
        final List<String> periodTypes = jdbcTemplate.query(dataPsr.getSql(), dataPsr.getSetter(), (rs,rowNum) -> {
            return rs.getString("period_type");
        });

        return periodTypes;
	}
}
