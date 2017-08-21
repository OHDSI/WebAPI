package org.ohdsi.webapi.cohortresults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.PathParam;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortresults.mapper.CohortAttributeMapper;
import org.ohdsi.webapi.cohortresults.mapper.CohortStatsMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptConditionCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDecileCountsMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDecileMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDistributionMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptObservationCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptQuartileMapper;
import org.ohdsi.webapi.cohortresults.mapper.CumulativeObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptEraMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptPrevalenceMapper;
import org.ohdsi.webapi.cohortresults.mapper.MonthObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.ObservationPeriodMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceConceptMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceConceptNameMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceMapper;
import org.ohdsi.webapi.cohortresults.mapper.ScatterplotMapper;
import org.ohdsi.webapi.cohortresults.mapper.SeriesPerPersonMapper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	private static final Log log = LogFactory.getLog(CohortResultsAnalysisRunner.class);
	
	public static final String BASE_SQL_PATH = "/resources/cohortresults/sql";

	private static final String[] STANDARD_COLUMNS = new String[]{"cdm_database_schema",
		"ohdsi_database_schema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount"};

	private static final String[] DRILLDOWN_COLUMNS = new String[]{"cdm_database_schema",
		"ohdsi_database_schema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount", "conceptId"};

	public static final String MIN_COVARIATE_PERSON_COUNT = "10";
	public static final String MIN_INTERVAL_PERSON_COUNT = "10";

	private ObjectMapper mapper;
	private String sourceDialect;
	private VisualizationDataRepository visualizationDataRepository;

	public CohortResultsAnalysisRunner(String sourceDialect, VisualizationDataRepository visualizationDataRepository) {
		this.sourceDialect = sourceDialect;
		this.visualizationDataRepository = visualizationDataRepository;

		mapper = new ObjectMapper();
	}

	public List<ScatterplotRecord> getCohortConditionDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source, 
			boolean save) {

		final String key = COHORT_SPECIFIC_CONDITION_DRILLDOWN;
		List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();

		final String sql = this.renderDrillDownCohortSql("firstConditionRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			records = jdbcTemplate.query(sql, new ScatterplotMapper());
			if (save) {
				this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}

		return records;
	}

	public CohortDataDensity getCohortDataDensity(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		CohortDataDensity data = new CohortDataDensity();
		final String key = DATA_DENSITY;
		boolean empty = true;

		String recordsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/recordsperperson.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (recordsPerPersonSql != null) {
			data.setRecordsPerPerson(jdbcTemplate.query(recordsPerPersonSql, new SeriesPerPersonMapper()));
		}
		String totalRecordsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/totalrecords.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (totalRecordsSql != null) {
			data.setTotalRecords(jdbcTemplate.query(totalRecordsSql, new SeriesPerPersonMapper()));
		}
		String conceptsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/conceptsperperson.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conceptsPerPersonSql != null) {
			data.setConceptsPerPerson(jdbcTemplate.query(conceptsPerPersonSql, new ConceptQuartileMapper()));
		}
		
		if (CollectionUtils.isNotEmpty(data.getRecordsPerPerson())
				|| CollectionUtils.isNotEmpty(data.getTotalRecords())
				|| CollectionUtils.isNotEmpty(data.getConceptsPerPerson())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntity(id, source.getSourceId(), key, data);
		}

		return data;
	}

	public CohortDeathData getCohortDeathData(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortDeathData data = new CohortDeathData();
		final String key = DEATH;
		boolean empty = true;

		List<ConceptQuartileRecord> age = null;
		String ageSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlAgeAtDeath.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageSql != null) {
			age = jdbcTemplate.query(ageSql, new ConceptQuartileMapper());
		}
		data.setAgetAtDeath(age);

		List<ConceptCountRecord> byType = null;
		String byTypeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlDeathByType.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql, new ConceptCountMapper());
		}
		data.setDeathByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByGenderAgeYear.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new ConceptDecileCountsMapper());
		}
		data.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByMonth.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
		final String key = COHORT_SPECIFIC_DRUG_DRILLDOWN;

		final String sql = this.renderDrillDownCohortSql("drugOccursRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			records = jdbcTemplate.query(sql, new ScatterplotMapper());
			if (save) {
				this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}

		return records;
	}

	public List<HierarchicalConceptRecord> getCohortMeasurementResults(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		List<HierarchicalConceptRecord> res = null;
		final String key = MEASUREMENT;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/measurement/sqlMeasurementTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}
		return res;
	}

	public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortMeasurementDrilldown drilldown = new CohortMeasurementDrilldown();
		final String key = MEASUREMENT_DRILLDOWN;
		boolean empty = true;

		String ageAtFirstOccurrenceSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstOccurrenceSql != null) {
			drilldown.setAgeAtFirstOccurrence(jdbcTemplate.query(ageAtFirstOccurrenceSql, new ConceptQuartileMapper()));
		}

		String sqlLowerLimitDistribution = this.renderDrillDownCohortSql("sqlLowerLimitDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlLowerLimitDistribution != null) {
			drilldown.setLowerLimitDistribution(jdbcTemplate.query(sqlLowerLimitDistribution, new ConceptQuartileMapper()));
		}

		String sqlMeasurementValueDistribution = this.renderDrillDownCohortSql("sqlMeasurementValueDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlMeasurementValueDistribution != null) {
			drilldown.setMeasurementValueDistribution(jdbcTemplate.query(sqlMeasurementValueDistribution, new ConceptQuartileMapper()));
		}

		String sqlUpperLimitDistribution = this.renderDrillDownCohortSql("sqlUpperLimitDistribution", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlUpperLimitDistribution != null) {
			drilldown.setUpperLimitDistribution(jdbcTemplate.query(sqlUpperLimitDistribution, new ConceptQuartileMapper()));
		}

		String sqlMeasurementsByType = this.renderDrillDownCohortSql("sqlMeasurementsByType", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlMeasurementsByType != null) {
			drilldown.setMeasurementsByType(jdbcTemplate.query(sqlMeasurementsByType, new ConceptObservationCountMapper()));
		}

		String sqlRecordsByUnit = this.renderDrillDownCohortSql("sqlRecordsByUnit", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlRecordsByUnit != null) {
			drilldown.setRecordsByUnit(jdbcTemplate.query(sqlRecordsByUnit, new ConceptObservationCountMapper()));
		}

		String sqlValuesRelativeToNorm = this.renderDrillDownCohortSql("sqlValuesRelativeToNorm", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlValuesRelativeToNorm != null) {
			drilldown.setValuesRelativeToNorm(jdbcTemplate.query(sqlValuesRelativeToNorm, new ConceptObservationCountMapper()));
		}

		String sqlPrevalenceByGenderAgeYear = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlPrevalenceByGenderAgeYear != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(sqlPrevalenceByGenderAgeYear, new ConceptDecileMapper()));
		}

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", MEASUREMENT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptNameMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortObservationPeriod obsPeriod = new CohortObservationPeriod();
		final String key = OBSERVATION_PERIOD;
		boolean empty = true;

		String ageAtFirstSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			obsPeriod.setAgeAtFirst(jdbcTemplate.query(ageAtFirstSql, new ConceptDistributionMapper()));
		}

		String obsLengthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_data.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsLengthSql != null) {
			obsPeriod.setObservationLength(jdbcTemplate.query(obsLengthSql, new ConceptDistributionMapper()));
		}

		String obsLengthStatsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsLengthStatsSql != null) {
			obsPeriod.setObservationLengthStats(jdbcTemplate.query(obsLengthStatsSql, new CohortStatsMapper()));
		}

		String obsYearStatsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsYearStatsSql != null) {
			obsPeriod.setPersonsWithContinuousObservationsByYearStats(jdbcTemplate.query(obsYearStatsSql, new CohortStatsMapper()));
		}

		String personsWithContObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_data.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsWithContObsSql != null) {
			obsPeriod.setPersonsWithContinuousObservationsByYear(jdbcTemplate.query(personsWithContObsSql, new ConceptDistributionMapper()));
		}

		String ageByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/agebygender.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageByGenderSql != null) {
			obsPeriod.setAgeByGender(jdbcTemplate.query(ageByGenderSql, new ConceptQuartileMapper()));
		}

		String durationByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbygender.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (durationByGenderSql != null) {
			obsPeriod.setDurationByGender(jdbcTemplate.query(durationByGenderSql, new ConceptQuartileMapper()));
		}

		String durationByAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbyage.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (durationByAgeSql != null) {
			obsPeriod.setDurationByAgeDecile(jdbcTemplate.query(durationByAgeSql, new ConceptQuartileMapper()));
		}

		String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (cumulObsSql != null) {
			obsPeriod.setCumulativeObservation(jdbcTemplate.query(cumulObsSql, new CumulativeObservationMapper()));
		}

		String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (obsByMonthSql != null) {
			obsPeriod.setObservedByMonth(jdbcTemplate.query(obsByMonthSql, new MonthObservationMapper()));
		}

		String obsPeriodsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/periodsperperson.sql", id, minCovariatePersonCountParam,
				minIntervalPersonCountParam, source);
		if (obsPeriodsPerPersonSql != null) {
			obsPeriod.setObservationPeriodsPerPerson(jdbcTemplate.query(obsPeriodsPerPersonSql, new ConceptCountMapper()));
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;

		final String key = OBSERVATION;
		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observation/sqlObservationTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortObservationDrilldown getCohortObservationResultsDrilldown(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conceptId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortObservationDrilldown drilldown = new CohortObservationDrilldown();
		final String key = OBSERVATION_DRILLDOWN;
		boolean empty = true;

		String ageAtFirstOccurrenceSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstOccurrenceSql != null) {
			drilldown.setAgeAtFirstOccurrence(jdbcTemplate.query(ageAtFirstOccurrenceSql, new ConceptQuartileMapper()));
		}

		String sqlObservationValueDistribution = this.renderDrillDownCohortSql("sqlObservationValueDistribution", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlObservationValueDistribution != null) {
			drilldown.setObservationValueDistribution(jdbcTemplate.query(sqlObservationValueDistribution, new ConceptQuartileMapper()));
		}

		String sqlObservationsByType = this.renderDrillDownCohortSql("sqlObservationsByType", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlObservationsByType != null) {
			drilldown.setObservationsByType(jdbcTemplate.query(sqlObservationsByType, new ConceptObservationCountMapper()));
		}

		String sqlRecordsByUnit = this.renderDrillDownCohortSql("sqlRecordsByUnit", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlRecordsByUnit != null) {
			drilldown.setRecordsByUnit(jdbcTemplate.query(sqlRecordsByUnit, new ConceptObservationCountMapper()));
		}

		String sqlPrevalenceByGenderAgeYear = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sqlPrevalenceByGenderAgeYear != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(sqlPrevalenceByGenderAgeYear, new ConceptDecileMapper()));
		}

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", OBSERVATION, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptNameMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
		final String key = COHORT_SPECIFIC_PROCEDURE_DRILLDOWN;

		final String sql = this.renderDrillDownCohortSql("procedureOccursRelativeToIndex", "cohortSpecific", id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			records = jdbcTemplate.query(sql, new ScatterplotMapper());
			if (save) {
				this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, records);
			}
		}
		return records;
	}

	public CohortProceduresDrillDown getCohortProceduresDrilldown(JdbcTemplate jdbcTemplate,
			final int id,
			final int conceptId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		CohortProceduresDrillDown drilldown = new CohortProceduresDrillDown();
		final String key = PROCEDURE_DRILLDOWN;
		boolean empty = true;
		
		List<ConceptQuartileRecord> ageAtFirst = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstOccurrence(ageAtFirst);

		List<ConceptCountRecord> byType = null;
		String byTypeSql = this.renderDrillDownCohortSql("sqlProceduresByType", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql, new ConceptCountMapper());
		}
		drilldown.setProceduresByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", PROCEDURE, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);
		
		if (CollectionUtils.isNotEmpty(drilldown.getAgeAtFirstOccurrence())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByGenderAgeYear())
				|| CollectionUtils.isNotEmpty(drilldown.getPrevalenceByMonth())
				|| CollectionUtils.isNotEmpty(drilldown.getProceduresByType())) {
			empty = false;
		}

		if (!empty && save) {
			this.saveEntityDrilldown(id, source.getSourceId(), key, conceptId, drilldown);
		}

		return drilldown;
	}

	public CohortSpecificSummary getCohortSpecificSummary(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam, 
			Source source, 
			boolean save) {
		final String key = COHORT_SPECIFIC;
		CohortSpecificSummary summary = new CohortSpecificSummary();
		boolean empty = true;
		
		// 1805, 1806
		String personsByDurationSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/observationPeriodTimeRelativeToIndex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsByDurationSql != null) {
			summary.setPersonsByDurationFromStartToEnd(jdbcTemplate.query(personsByDurationSql, new ObservationPeriodMapper()));
		}

		// 1815
		String monthPrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByMonth.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (monthPrevalenceSql != null) {
			summary.setPrevalenceByMonth(jdbcTemplate.query(monthPrevalenceSql, new PrevalanceMapper()));
		}

		// 1814
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByYearGenderSex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new ConceptDecileCountsMapper());
		}
		summary.setNumPersonsByCohortStartByGenderByAge(prevalenceByGenderAgeYear);

		// 1801
		List<ConceptQuartileRecord> ageAtIndex = null;
		String ageAtIndexSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/ageAtIndexDistribution.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			ageAtIndex = jdbcTemplate.query(ageAtIndexSql, new ConceptQuartileMapper());
		}
		summary.setAgeAtIndexDistribution(ageAtIndex);

		// 1803
		List<ConceptQuartileRecord> distributionAgeCohortStartByCohortStartYear = null;
		String distributionAgeCohortStartByCohortStartYearSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByCohortStartYear.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			distributionAgeCohortStartByCohortStartYear = jdbcTemplate.query(distributionAgeCohortStartByCohortStartYearSql, new ConceptQuartileMapper());
		}
		summary.setDistributionAgeCohortStartByCohortStartYear(distributionAgeCohortStartByCohortStartYear);

		// 1802
		List<ConceptQuartileRecord> distributionAgeCohortStartByGender = null;
		String distributionAgeCohortStartByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByGender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtIndexSql != null) {
			distributionAgeCohortStartByGender = jdbcTemplate.query(distributionAgeCohortStartByGenderSql, new ConceptQuartileMapper());
		}
		summary.setDistributionAgeCohortStartByGender(distributionAgeCohortStartByGender);

		// 1804
		String personsInCohortFromCohortStartToEndSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/personsInCohortFromCohortStartToEnd.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (personsInCohortFromCohortStartToEndSql != null) {
			summary.setPersonsInCohortFromCohortStartToEnd(jdbcTemplate.query(personsInCohortFromCohortStartToEndSql, new MonthObservationMapper()));
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam, Source source,
			boolean save) {

		final String key = COHORT_SPECIFIC_TREEMAP;
		CohortSpecificTreemap summary = new CohortSpecificTreemap();
		boolean empty = true;
		
		// 1820
		String conditionOccurrencePrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/conditionOccurrencePrevalenceOfCondition.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conditionOccurrencePrevalenceSql != null) {
			summary.setConditionOccurrencePrevalence(jdbcTemplate.query(conditionOccurrencePrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
		}

		// 1830
		String procedureOccurrencePrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/procedureOccurrencePrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (procedureOccurrencePrevalenceSql != null) {
			summary.setProcedureOccurrencePrevalence(jdbcTemplate.query(procedureOccurrencePrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
		}

		// 1870
		String drugEraPrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/drugEraPrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (drugEraPrevalenceSql != null) {
			summary.setDrugEraPrevalence(jdbcTemplate.query(drugEraPrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		CohortVisitsDrilldown drilldown = new CohortVisitsDrilldown();
		final String key = VISIT_DRILLDOWN;
		boolean empty = true;

		List<ConceptQuartileRecord> ageAtFirst = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstOccurrence(ageAtFirst);

		List<ConceptQuartileRecord> byType = null;
		String byTypeSql = this.renderDrillDownCohortSql("sqlVisitDurationByType", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (byTypeSql != null) {
			byType = jdbcTemplate.query(byTypeSql, new ConceptQuartileMapper());
		}
		drilldown.setVisitDurationByType(byType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", VISIT, id,
				conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptMapper());
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
			@PathParam("conditionId") final int conditionId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION_ERA_DRILLDOWN;
		CohortConditionEraDrilldown drilldown = new CohortConditionEraDrilldown();
		boolean empty = true;
		
		// age at first diagnosis
		List<ConceptQuartileRecord> ageAtFirst = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirst = jdbcTemplate.query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirst);

		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (lengthOfEraSql != null) {
			lengthOfEra = jdbcTemplate.query(lengthOfEraSql, new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);

		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "conditionera", id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalenceByMonthSql, new PrevalanceConceptMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION_ERA;
		List<HierarchicalConceptRecord> res = null;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/conditionera/sqlConditionEraTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptEraMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortConditionDrilldown getConditionResults(JdbcTemplate jdbcTemplate,
			final int id, 
			final int conditionId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		final String key = CONDITION_DRILLDOWN;
		CohortConditionDrilldown drilldown = new CohortConditionDrilldown();
		boolean empty = true;

		List<ConceptQuartileRecord> ageAtFirstDiagnosis = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstSql != null) {
			ageAtFirstDiagnosis = jdbcTemplate.query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirstDiagnosis);

		List<ConceptCountRecord> conditionsByType = null;
		String conditionsSql = this.renderDrillDownCohortSql("sqlConditionsByType", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (conditionsSql != null) {
			conditionsByType = jdbcTemplate.query(conditionsSql, new ConceptConditionCountMapper());
		}
		drilldown.setConditionsByType(conditionsByType);

		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", CONDITION, id,
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new PrevalanceConceptMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source, boolean save) {

		final String key = CONDITION;
		List<HierarchicalConceptRecord> res = null;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/condition/sqlConditionTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortDashboard getDashboard(JdbcTemplate jdbcTemplate, 
			int id, Source source,
			String minCovariatePersonCountParam, String minIntervalPersonCountParam, 
			boolean demographicsOnly,
			boolean save) {
		
		final String key = DASHBOARD;
		CohortDashboard dashboard = new CohortDashboard();
		boolean empty = true;
		
		String ageAtFirstObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", id,
				minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstObsSql != null) {
			dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql, new ConceptDistributionMapper()));
		}

		String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam,
				minIntervalPersonCountParam, source);
		if (genderSql != null) {
			dashboard.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
		}

		if (!demographicsOnly) {
			String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
					minCovariatePersonCountParam, minIntervalPersonCountParam, source);
			if (cumulObsSql != null) {
				dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql, new CumulativeObservationMapper()));
			}

			String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
					minCovariatePersonCountParam, minIntervalPersonCountParam, source);
			if (obsByMonthSql != null) {
				dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql, new MonthObservationMapper()));
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {
		
		final String key = DRUG_ERA_DRILLDOWN;
		CohortDrugEraDrilldown drilldown = new CohortDrugEraDrilldown();
		boolean empty = true;

		// age at first exposure
		List<ConceptQuartileRecord> ageAtFirstExposure = null;
		String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstExposureSql != null) {
			ageAtFirstExposure = jdbcTemplate.query(ageAtFirstExposureSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstExposure(ageAtFirstExposure);

		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (lengthOfEraSql != null) {
			lengthOfEra = jdbcTemplate.query(lengthOfEraSql, new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);

		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "drugera", id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = jdbcTemplate.query(prevalenceByMonthSql, new PrevalanceConceptMapper());
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = DRUG_ERA;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drugera/sqlDrugEraTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptEraMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public CohortDrugDrilldown getDrugResults(JdbcTemplate jdbcTemplate, 
			final int id, 
			final int drugId,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		final String key = DRUG_DRILLDOWN;
		CohortDrugDrilldown drilldown = new CohortDrugDrilldown();
		boolean empty = true;

		String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ageAtFirstExposureSql != null) {
			drilldown.setAgeAtFirstExposure(jdbcTemplate.query(ageAtFirstExposureSql, new ConceptQuartileMapper()));
		}

		String daysSupplySql = this.renderDrillDownCohortSql("sqlDaysSupplyDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (daysSupplySql != null) {
			drilldown.setDaysSupplyDistribution(jdbcTemplate.query(daysSupplySql, new ConceptQuartileMapper()));
		}

		String drugsByTypeSql = this.renderDrillDownCohortSql("sqlDrugsByType", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (drugsByTypeSql != null) {
			drilldown.setDrugsByType(jdbcTemplate.query(drugsByTypeSql, new ConceptCountMapper()));
		}

		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByGenderAgeYearSql != null) {
			drilldown.setPrevalenceByGenderAgeYear(jdbcTemplate.query(prevalenceByGenderAgeYearSql,
					new ConceptDecileMapper()));
		}

		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (prevalenceByMonthSql != null) {
			drilldown.setPrevalenceByMonth(jdbcTemplate.query(prevalenceByMonthSql, new PrevalanceConceptMapper()));
		}

		String quantityDistributionSql = this.renderDrillDownCohortSql("sqlQuantityDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (quantityDistributionSql != null) {
			drilldown.setQuantityDistribution(jdbcTemplate.query(quantityDistributionSql, new ConceptQuartileMapper()));
		}

		String refillsDistributionSql = this.renderDrillDownCohortSql("sqlRefillsDistribution", DRUG, id,
				drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (refillsDistributionSql != null) {
			drilldown.setRefillsDistribution(jdbcTemplate.query(refillsDistributionSql, new ConceptQuartileMapper()));
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
			this.saveEntityDrilldown(id, source.getSourceId(), key, drugId, drilldown);
		}

		return drilldown;

	}

	public List<HierarchicalConceptRecord> getDrugTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source, boolean save) {


		final String key = DRUG;
		List<HierarchicalConceptRecord> res = null;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drug/sqlDrugTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}


		return res;
	}

	public List<CohortAttribute> getHeraclesHeel(JdbcTemplate jdbcTemplate,
			final int id, 
			Source source,
			boolean save) {
		List<CohortAttribute> attrs = new ArrayList<CohortAttribute>();
		final String key = HERACLES_HEEL;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/heraclesHeel/sqlHeraclesHeel.sql", id, null, null, source);
		if (sql != null) {
			attrs = jdbcTemplate.query(sql, new CohortAttributeMapper());
		}
		if (save) {
			this.saveEntity(id, source.getSourceId(), key, attrs);
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			final Source source,
			boolean save) {
		
		final String key = PERSON;
		CohortPersonSummary person = new CohortPersonSummary();
		boolean empty = true;
		
		String yobSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_data.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (yobSql != null) {
			person.setYearOfBirth(jdbcTemplate.query(yobSql, new ConceptDistributionMapper()));
		}

		String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (yobStatSql != null) {
			person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql, new CohortStatsMapper()));
		}

		String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (genderSql != null) {
			person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
		}

		String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (raceSql != null) {
			person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
		}

		String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (ethnicitySql != null) {
			person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
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
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = PROCEDURE;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/procedure/sqlProcedureTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	public List<HierarchicalConceptRecord> getVisitTreemap(JdbcTemplate jdbcTemplate,
			final int id,
			final String minCovariatePersonCountParam,
			final String minIntervalPersonCountParam,
			Source source,
			boolean save) {

		List<HierarchicalConceptRecord> res = null;
		final String key = VISIT;

		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/visit/sqlVisitTreemap.sql",
				id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
		if (sql != null) {
			res = jdbcTemplate.query(sql, new HierarchicalConceptMapper());
			if (save) {
				this.saveEntity(id, source.getSourceId(), key, res);
			}
		}

		return res;
	}

	// HELPER methods
	/**
	 * Renders and Translates drilldown SQL by concept
	 *
	 * @param analysisName
	 * @param analysisType
	 * @param id
	 * @param conceptId
	 * @param minCovariatePersonCountParam
	 * @param minIntervalPersonCountParam
	 * @return
	 */
	
	public String renderDrillDownCohortSql(String analysisName, String analysisType, int id, int conceptId,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam, Source source) {
		return renderTranslateCohortSql(BASE_SQL_PATH + "/" + analysisType + "/byConcept/" + analysisName + ".sql",
				id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
	}


	/**
	 * Passes in common params for cohort results, and performs SQL
	 * translate/render
	 */
	public String renderTranslateCohortSql(String sqlPath, Integer id, Integer conceptId,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam,
			Source source) {
		String sql = null;

		String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
		String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

		try {
			String[] cols;
			String[] colValues;
			if (conceptId != null) {
				cols = DRILLDOWN_COLUMNS;
				colValues = new String[]{vocabularyTableQualifier,
						resultsTableQualifier, String.valueOf(id),
						minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
								: minCovariatePersonCountParam,
								minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
										: minIntervalPersonCountParam,
										String.valueOf(conceptId)};
			} else {
				cols = STANDARD_COLUMNS;
				colValues = new String[]{vocabularyTableQualifier,
						resultsTableQualifier, String.valueOf(id),
						minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
								: minCovariatePersonCountParam,
								minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
										: minIntervalPersonCountParam};
			}

			sql = ResourceHelper.GetResourceAsString(sqlPath);
			sql = SqlRender.renderSql(sql, cols, colValues);
			sql = SqlTranslate.translateSql(sql, sourceDialect, source.getSourceDialect());
		} catch (Exception e) {
			log.error(String.format("Unable to translate sql for  %s", sql), e);
		}

		return sql;
	}

	/**
	 * Passes in common params for cohort results, and performs SQL
	 * translate/render
	 */
	public String renderTranslateCohortSql(String sqlPath, Integer id,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam, Source source) {
		return renderTranslateCohortSql(sqlPath, id, null, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
	}

	private void saveEntity(int cohortDefinitionId, int sourceId, String visualizationKey, Object dataObject) {
		if (dataObject == null) {
			log.error(String.format("cannot store null entity %s",  visualizationKey));
			return;
		}
		
		if (dataObject instanceof List) {
			List<?> listObject = (List<?>) dataObject;
			if (listObject.size() == 0) {
				log.debug(String.format("no need to store empty list for %s",  visualizationKey));
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
			log.error(e);
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
			log.error(e);
		}
	}

	private void saveEntityDrilldown(int cohortDefinitionId, int sourceId, String visualizationKey, int drilldownId, Object dataObject) {
		if (dataObject == null) {
			log.error(String.format("cannot store null entity %s",  visualizationKey));
			return;
		}
		
		if (dataObject instanceof List) {
			List<?> listObject = (List<?>) dataObject;
			if (listObject.size() == 0) {
				log.debug(String.format("no need to store empty list for %s",  visualizationKey));
				return;
			}
		}
		
		// delete the old one
		try {
			this.visualizationDataRepository
				.deleteByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(cohortDefinitionId, sourceId, visualizationKey, drilldownId);
		} catch (Exception e) {
			log.error(e);
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
			log.error(e);
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
}
