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
	 * @param analysis_name Name of the analysis, currently the same name as under the /resources/cohortresults/sql/ directory
	 * @return a List of key, value pairs
	 */
	  @GET
	  @Path("/{id}/{analysis_name}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public List<Map<String, String>> getCohortResults(@PathParam("id") final int id, @PathParam("analysis_name") final String analysisName,
			  @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			  @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		  List<Map<String, String>> results = null;
		  
	      String sql = null;
	      
	      try {
	    	  sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/" + analysisName + ".sql");

	    	  sql = SqlRender.renderSql(sql, new String[] { "cdmSchema", 
	    			  	"resultsSchema", "heraclesResultsTable", "heraclesResultsDistTable",
	    			  	"achillesResultsTable", "achillesResultsDistTable", "cohortDefinitionId",
	    			  	"minCovariatePersonCount", "minIntervalPersonCount"},
	    	          new String[] { this.getCdmSchema(),
	    			  	this.getOhdsiSchema(), this.getHeraclesResultsTable(), this.getHeraclesResultsDistTable(),
	    			  	this.getAchillesResultsTable(), this.getAchillesResultsDistTable(), String.valueOf(id),
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
}
 