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
import org.ohdsi.webapi.cohortresults.ConditionDrilldown;
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
		 * Queries for cohort analysis condition drilldown results for the given cohort definition id and condition id
		 * 
		 * @param id cohort_defintion id
		 * @param conditionId condition_id (from concept)
		 * @return ConditionDrilldown
		 */
		  @GET
		  @Path("/{id}/condition/{conditionId}")
		  @Produces(MediaType.APPLICATION_JSON)
		  public ConditionDrilldown getConditionResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
				  @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
				  @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
			  ConditionDrilldown conditionDrilldown = new ConditionDrilldown();
			  
			  conditionDrilldown.setAgeAtFirstDiagnosis(
					  this.getConditionDrillDownResults("sqlAgeAtFirstDiagnosis", id, conditionId, 
							  minCovariatePersonCountParam, minIntervalPersonCountParam));
			  conditionDrilldown.setConditionsByType(this.getConditionDrillDownResults("sqlConditionsByType", id, conditionId, 
							  minCovariatePersonCountParam, minIntervalPersonCountParam));
			  conditionDrilldown.setPrevalenceByGenderAgeYear(this.getConditionDrillDownResults("sqlPrevalenceByGenderAgeYear", id, conditionId, 
							  minCovariatePersonCountParam, minIntervalPersonCountParam));
			  conditionDrilldown.setPrevalenceByMonth(this.getConditionDrillDownResults("sqlPrevalenceByMonth", id, conditionId, 
							  minCovariatePersonCountParam, minIntervalPersonCountParam));
			  
			  
			  return conditionDrilldown;
			  
		  }
		  
		  private List<Map<String, String>> getConditionDrillDownResults(String analysisName, int id, int conceptId,
				  final String minCovariatePersonCountParam, final String minIntervalPersonCountParam) {
			  List<Map<String, String>> results = null;
			  
		      String sql = null;
		      
		      try {
		    	  sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/condition/byConcept/" + analysisName + ".sql");

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
 