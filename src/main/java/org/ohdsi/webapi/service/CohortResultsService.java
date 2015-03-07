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
			sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/" + analysisGroup + "/" + analysisName + ".sql");

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

		dashboard.setAgeAtFirstObservation(this.getCohortResultsRaw(id, "observationperiod", "ageatfirst", minCovariatePersonCountParam, minIntervalPersonCountParam));
		dashboard.setCumulativeObservation(this.getCohortResultsRaw(id, "observationperiod", "cumulativeduration", minCovariatePersonCountParam, minIntervalPersonCountParam));
		dashboard.setGender(this.getCohortResultsRaw(id, "person", "gender", minCovariatePersonCountParam, minIntervalPersonCountParam));
		dashboard.setObservedByMonth(this.getCohortResultsRaw(id, "observationperiod", "observedbymonth", minCovariatePersonCountParam, minIntervalPersonCountParam));

		return dashboard;

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
		CohortConditionDrilldown conditionDrilldown = new CohortConditionDrilldown();

		conditionDrilldown.setAgeAtFirstDiagnosis(
				this.getDrillDownResults("sqlAgeAtFirstDiagnosis", "condition", id, conditionId, 
						minCovariatePersonCountParam, minIntervalPersonCountParam));
		conditionDrilldown.setConditionsByType(this.getDrillDownResults("sqlConditionsByType","condition", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		conditionDrilldown.setPrevalenceByGenderAgeYear(this.getDrillDownResults("sqlPrevalenceByGenderAgeYear", "condition", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		conditionDrilldown.setPrevalenceByMonth(this.getDrillDownResults("sqlPrevalenceByMonth", "condition", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));


		return conditionDrilldown;

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
		drilldown.setAgeAtFirstDiagnosis(this.getDrillDownResults("sqlAgeAtFirstDiagnosis", "conditionera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setLengthOfEra(this.getDrillDownResults("sqlLengthOfEra", "conditionera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByGenderAgeYear(this.getDrillDownResults("sqlPrevalenceByGenderAgeYear", "conditionera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByMonth(this.getDrillDownResults("sqlPrevalenceByMonth", "conditionera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));


		return drilldown;

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

		drilldown.setAgeAtFirstExposure(this.getDrillDownResults("sqlAgeAtFirstExposure", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setDaysSupplyDistribution(this.getDrillDownResults("sqlDaysSupplyDistribution", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setDrugsByType(this.getDrillDownResults("sqlDrugsByType", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByGenderAgeYear(this.getDrillDownResults("sqlPrevalenceByGenderAgeYear", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByMonth(this.getDrillDownResults("sqlPrevalenceByMonth", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setQuantityDistribution(this.getDrillDownResults("sqlQuantityDistribution", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setRefillsDistribution(this.getDrillDownResults("sqlRefillsDistribution", "drug", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));


		return drilldown;

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

		drilldown.setAgeAtFirstExposure(this.getDrillDownResults("sqlAgeAtFirstExposure", "drugera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setLengthOfEra(this.getDrillDownResults("sqlLengthOfEra", "drugera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByGenderAgeYear(this.getDrillDownResults("sqlPrevalenceByGenderAgeYear", "drugera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));
		drilldown.setPrevalenceByMonth(this.getDrillDownResults("sqlPrevalenceByMonth", "drugera", id, conditionId, 
				minCovariatePersonCountParam, minIntervalPersonCountParam));

		return drilldown;

	}

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

		String sql = null;

		try {
			sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/" + analysisType + "/byConcept/" + analysisName + ".sql");

			sql = SqlRender.renderSql(sql, new String[] { "cdmSchema", 
					"resultsSchema", "cohortDefinitionId",
					"minCovariatePersonCount", "minIntervalPersonCount", "conceptId"},
					new String[] { this.getCdmSchema(),
					this.getOhdsiSchema(), String.valueOf(id),
					minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT :
						minCovariatePersonCountParam, 
						minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT :
							minIntervalPersonCountParam,
							String.valueOf(conceptId)});
			sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
		} catch (Exception e) {
			log.error(String.format("Unable to translate sql for analysis %s", analysisName), e);
		}

		if (sql != null) {
			results = genericResultSetLoader(sql);
		}

		return results;
	}
}
