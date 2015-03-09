package org.ohdsi.webapi.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.CohortConditionDrilldown;
import org.ohdsi.webapi.cohortresults.CohortConditionEraDrilldown;
import org.ohdsi.webapi.cohortresults.CohortDashboard;
import org.ohdsi.webapi.cohortresults.CohortDrugDrilldown;
import org.ohdsi.webapi.cohortresults.CohortDrugEraDrilldown;
import org.ohdsi.webapi.cohortresults.ConceptCountRecord;
import org.ohdsi.webapi.cohortresults.ConceptDecileRecord;
import org.ohdsi.webapi.cohortresults.ConceptQuartileRecord;
import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.ohdsi.webapi.cohortresults.PrevalenceRecord;
import org.ohdsi.webapi.cohortresults.mapper.ConceptConditionCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDecileMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDistributionMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptQuartileMapper;
import org.ohdsi.webapi.cohortresults.mapper.CumulativeObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptEraMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptMapper;
import org.ohdsi.webapi.cohortresults.mapper.MonthObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceMapper;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.springframework.stereotype.Component;

/**
 * 
 * Services related to viewing Heracles analyses
 *
 */
@Path("/cohortresults/")
@Component
public class CohortResultsService extends AbstractDaoService {

	private static final String MIN_COVARIATE_PERSON_COUNT = "500";
	private static final String MIN_INTERVAL_PERSON_COUNT = "1000";
	
	private static final String BASE_SQL_PATH = "/resources/cohortresults/sql";
	
	private static final String[] STANDARD_COLUMNS = new String[] { "cdmSchema", 
		"resultsSchema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount"};
	private static final String[] DRILLDOWN_COLUMNS = new String[] { "cdmSchema", 
		"resultsSchema", "cohortDefinitionId",
		"minCovariatePersonCount", "minIntervalPersonCount", "conceptId"};

	/**
	 * Queries for cohort analysis results for the given cohort definition id
	 * @param id cohort_defintion id
	 * @param analysisGroup Name of the analysisGrouping under the /resources/cohortresults/sql/ directory
	 * @param analysisName Name of the analysis, currently the same name as the sql file under analysisGroup
	 * @return List of key, value pairs
	 */
	@GET
	@Path("/{id}/raw/{analysis_group}/{analysis_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, String>> getCohortResultsRaw(@PathParam("id") final int id, @PathParam("analysis_group") final String analysisGroup,
			@PathParam("analysis_name") final String analysisName,
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		List<Map<String, String>> results = null;

		String sql = null;

		try {
			sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/" + analysisGroup + "/" + analysisName + ".sql");

			sql = SqlRender.renderSql(sql, new String[] { "cdmSchema", 
					"resultsSchema", "cohortDefinitionId",
					"minCovariatePersonCount", "minIntervalPersonCount"},
					new String[] { this.getCdmSchema(),
					this.getOhdsiSchema(), String.valueOf(id),
					minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT :
						minCovariatePersonCountParam, 
						minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT :
							minIntervalPersonCountParam});
			sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
		} catch (Exception e) {
			log.error(String.format("Unable to translate sql for analysis %s", analysisName), e);
		}

		if (sql != null) {
			results = genericResultSetLoader(sql);
		}

		return results;
	}

	/**
	 * Queries for cohort analysis dashboard for the given cohort definition id
	 * 
	 * @param id cohort_defintion id
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortDashboard getConditionResults(@PathParam("id") final int id, 
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		CohortDashboard dashboard = new CohortDashboard();
		
		String ageAtFirstObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (ageAtFirstObsSql != null) {
			dashboard.setAgeAtFirstObservation(this.getJdbcTemplate().query(ageAtFirstObsSql, new ConceptDistributionMapper()));
		}
		
		String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (cumulObsSql != null) {
			dashboard.setCumulativeObservation(this.getJdbcTemplate().query(cumulObsSql, new CumulativeObservationMapper()));
		}
		
		String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (genderSql != null) {
			dashboard.setGender(this.getJdbcTemplate().query(genderSql, new ConceptCountMapper()));
		}
		
		String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (obsByMonthSql != null) {
			dashboard.setObservedByMonth(this.getJdbcTemplate().query(obsByMonthSql, new MonthObservationMapper()));
		}

		return dashboard;

	}
	
	/**
	 * Queries for cohort analysis condition treemap results for the given cohort definition id
	 * 
	 * @param id cohort_defintion id
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/condition/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HierarchicalConceptRecord> getConditionTreemap(@PathParam("id") final int id, 
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		
		
		List<HierarchicalConceptRecord> res = null;
		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/condition/sqlConditionTreemap.sql", 
				id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (sql != null) {
			res = getJdbcTemplate().query(sql, new HierarchicalConceptMapper());
		}
		
		return res;
	}

	/**
	 * Queries for cohort analysis condition drilldown results for the given cohort definition id and condition id
	 * 
	 * @param id cohort_defintion id
	 * @param conditionId condition_id (from concept)
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/condition/{conditionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortConditionDrilldown getConditionResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		CohortConditionDrilldown drilldown = new CohortConditionDrilldown();

		List<ConceptQuartileRecord> ageAtFirstDiagnosis = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "condition", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (ageAtFirstSql != null) {
			ageAtFirstDiagnosis = getJdbcTemplate().query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirstDiagnosis);

		List<ConceptCountRecord> conditionsByType = null;
		String conditionsSql = this.renderDrillDownCohortSql("sqlConditionsByType", "condition", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (conditionsSql != null) {
			conditionsByType = getJdbcTemplate().query(conditionsSql, new ConceptConditionCountMapper());
		}
		drilldown.setConditionsByType(conditionsByType);
		
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "condition", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceGenderAgeSql != null) {
			prevalenceByGenderAgeYear = getJdbcTemplate().query(prevalenceGenderAgeSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);
		
		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "condition", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalanceMonthSql != null) {
			prevalenceByMonth = getJdbcTemplate().query(prevalanceMonthSql, new PrevalanceMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		return drilldown;

	}
	
	/**
	 * Queries for cohort analysis condition era treemap results for the given cohort definition id
	 * 
	 * @param id cohort_defintion id
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/conditionera/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HierarchicalConceptRecord> getConditionEraTreemap(@PathParam("id") final int id, 
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		
		
		List<HierarchicalConceptRecord> res = null;
		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/conditionera/sqlConditionEraTreemap.sql", 
				id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (sql != null) {
			res = getJdbcTemplate().query(sql, new HierarchicalConceptEraMapper());
		}
		
		return res;
	}

	/**
	 * Queries for cohort analysis condition era drilldown results for the given cohort definition id and condition id
	 * 
	 * @param id cohort_defintion id
	 * @param conditionId condition_id (from concept)
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/conditionera/{conditionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortConditionEraDrilldown getConditionEraResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		CohortConditionEraDrilldown drilldown = new CohortConditionEraDrilldown();
		
		// age at first diagnosis
		List<ConceptQuartileRecord> ageAtFirst = null;
		String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "conditionera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (ageAtFirstSql != null) {
			ageAtFirst = getJdbcTemplate().query(ageAtFirstSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstDiagnosis(ageAtFirst);		
				
		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "conditionera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (lengthOfEraSql != null) {
			lengthOfEra = getJdbcTemplate().query(lengthOfEraSql, new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);
		
		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "conditionera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = getJdbcTemplate().query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);
		
		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "conditionera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = getJdbcTemplate().query(prevalenceByMonthSql, new PrevalanceMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		return drilldown;

	}
	
	/**
	 * Queries for drug analysis condition treemap results for the given cohort definition id
	 * 
	 * @param id cohort_defintion id
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/drug/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HierarchicalConceptRecord> getDrugTreemap(@PathParam("id") final int id, 
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		
		
		List<HierarchicalConceptRecord> res = null;
		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drug/sqlDrugTreemap.sql", 
				id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (sql != null) {
			res = getJdbcTemplate().query(sql, new HierarchicalConceptMapper());
		}
		
		return res;
	}

	/**
	 * Queries for cohort analysis drug drilldown results for the given cohort definition id and condition id
	 * 
	 * @param id cohort_defintion id
	 * @param conditionId condition_id (from concept)
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/drug/{conditionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortDrugDrilldown getDrugResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		CohortDrugDrilldown drilldown = new CohortDrugDrilldown();

		String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (ageAtFirstExposureSql != null) {
			drilldown.setAgeAtFirstExposure(this.getJdbcTemplate().query(ageAtFirstExposureSql, new ConceptQuartileMapper()));
		}
		
		String daysSupplySql = this.renderDrillDownCohortSql("sqlDaysSupplyDistribution", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (daysSupplySql != null) {
			drilldown.setDaysSupplyDistribution(this.getJdbcTemplate().query(daysSupplySql, new ConceptQuartileMapper()));
		}
		
		String drugsByTypeSql = this.renderDrillDownCohortSql("sqlDrugsByType", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (drugsByTypeSql != null) {
			drilldown.setDrugsByType(this.getJdbcTemplate().query(drugsByTypeSql, new ConceptCountMapper()));
		}
		
		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByGenderAgeYearSql != null) {
			drilldown.setPrevalenceByGenderAgeYear(this.getJdbcTemplate().query(prevalenceByGenderAgeYearSql, 
					new ConceptDecileMapper()));
		}
		
		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByMonthSql != null) {
			drilldown.setPrevalenceByMonth(this.getJdbcTemplate().query(prevalenceByMonthSql, new PrevalanceMapper()));
		}
		
		String quantityDistributionSql = this.renderDrillDownCohortSql("sqlQuantityDistribution", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (quantityDistributionSql != null) {
			drilldown.setQuantityDistribution(this.getJdbcTemplate().query(quantityDistributionSql, new ConceptQuartileMapper()));
		}
		
		String refillsDistributionSql = this.renderDrillDownCohortSql("sqlRefillsDistribution", "drug", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (refillsDistributionSql != null) {
			drilldown.setRefillsDistribution(this.getJdbcTemplate().query(refillsDistributionSql, new ConceptQuartileMapper()));
		}

		return drilldown;

	}
	
	/**
	 * Queries for cohort analysis drug era treemap results for the given cohort definition id
	 * 
	 * @param id cohort_defintion id
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/drugera/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HierarchicalConceptRecord> getDrugEraTreemap(@PathParam("id") final int id, 
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		
		
		List<HierarchicalConceptRecord> res = null;
		String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drugera/sqlDrugEraTreemap.sql", 
				id, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (sql != null) {
			res = getJdbcTemplate().query(sql, new HierarchicalConceptEraMapper());
		}
		
		return res;
	}

	/**
	 * Queries for cohort analysis drug era drilldown results for the given cohort definition id and condition id
	 * 
	 * @param id cohort_defintion id
	 * @param conditionId condition_id (from concept)
	 * @return ConditionDrilldown
	 */
	@GET
	@Path("/{id}/drugera/{conditionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public CohortDrugEraDrilldown getDrugEraResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
			@QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			@QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		CohortDrugEraDrilldown drilldown = new CohortDrugEraDrilldown();

		// age at first exposure
		List<ConceptQuartileRecord> ageAtFirstExposure = null;
		String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", "drugera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (ageAtFirstExposureSql != null) {
			ageAtFirstExposure = getJdbcTemplate().query(ageAtFirstExposureSql, new ConceptQuartileMapper());
		}
		drilldown.setAgeAtFirstExposure(ageAtFirstExposure);		
				
		// length of era
		List<ConceptQuartileRecord> lengthOfEra = null;
		String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "drugera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (lengthOfEraSql != null) {
			lengthOfEra = getJdbcTemplate().query(lengthOfEraSql, new ConceptQuartileMapper());
		}
		drilldown.setLengthOfEra(lengthOfEra);
		
		// prevalence by gender age year
		List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
		String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drugera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByGenderAgeYearSql != null) {
			prevalenceByGenderAgeYear = getJdbcTemplate().query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
		}
		drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);
		
		// prevalence by month
		List<PrevalenceRecord> prevalenceByMonth = null;
		String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "drugera", id, 
				conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam);
		if (prevalenceByMonthSql != null) {
			prevalenceByMonth = getJdbcTemplate().query(prevalenceByMonthSql, new PrevalanceMapper());
		}
		drilldown.setPrevalenceByMonth(prevalenceByMonth);

		return drilldown;

	}
	
	// HELPER methods

	/**
	 * Retrieves concept specific drilldown results
	 * 
	 * @param analysisName
	 * @param analysisType
	 * @param id
	 * @param conceptId
	 * @param minCovariatePersonCountParam
	 * @param minIntervalPersonCountParam
	 * @return
	 */
	private List<Map<String, String>> getDrillDownResults(String analysisName, String analysisType, int id, int conceptId,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam) {
		List<Map<String, String>> results = null;

		String sql = renderDrillDownCohortSql(analysisName, analysisType, id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam);

		if (sql != null) {
			results = genericResultSetLoader(sql);
		}

		return results;
	}
	
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
	private String renderDrillDownCohortSql(String analysisName, String analysisType, int id, int conceptId,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam) {
		return renderTranslateCohortSql(BASE_SQL_PATH + "/" + analysisType + "/byConcept/" + analysisName + ".sql",
				id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam);
	}
	
	/**
	 * Passes in common params for cohort results, and performs SQL translate/render
	 */
	private String renderTranslateCohortSql(String sqlPath, Integer id, 
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam) {
		return renderTranslateCohortSql(sqlPath, id, null, minCovariatePersonCountParam, minIntervalPersonCountParam);
	}
	
	/**
	 * Passes in common params for cohort results, and performs SQL translate/render
	 */
	private String renderTranslateCohortSql(String sqlPath, Integer id, Integer conceptId,
			final String minCovariatePersonCountParam, final String minIntervalPersonCountParam) {
		String sql = null;

		try {
			String[] cols;
			String[] colValues;
			if (conceptId != null) {
				cols = DRILLDOWN_COLUMNS;
				colValues = new String[] { this.getCdmSchema(),
						this.getOhdsiSchema(), String.valueOf(id),
						minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT :
							minCovariatePersonCountParam, 
							minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT :
								minIntervalPersonCountParam,
								String.valueOf(conceptId)};
			} else {
				cols = STANDARD_COLUMNS;
				colValues = new String[] { this.getCdmSchema(),
						this.getOhdsiSchema(), String.valueOf(id),
						minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT :
							minCovariatePersonCountParam, 
							minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT :
								minIntervalPersonCountParam};
			}
			
			sql = ResourceHelper.GetResourceAsString(sqlPath);
			sql = SqlRender.renderSql(sql, cols, colValues);
			sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
		} catch (Exception e) {
			log.error(String.format("Unable to translate sql for  %s", sql), e);
		}
		
		return sql;
	}
}
;   