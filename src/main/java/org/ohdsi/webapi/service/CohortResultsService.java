package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Path("/cohortresults/")
@Component
public class CohortResultsService extends AbstractDaoService {
	
	private static final String MIN_COVARIATE_PERSON_COUNT = "500";
	private static final String MIN_INTERVAL_PERSON_COUNT = "1000";

	/**
	 * Queries for cohort analysis results for the given cohort definition id
	 * @param id
	 * @param analysis_id
	 * @return
	 */
	  @GET
	  @Path("/{id}/{analysis_id}")
	  @Produces(MediaType.APPLICATION_JSON)
	  public List<Map<String, String>> getCohortResults(@PathParam("id") final int id, @PathParam("analysis_id") final int analysisId,
			  @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam, 
			  @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
		  List<Map<String, String>> results= null;
		  
	      String sql = null;
	      
	      try {
	    	  sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/" + String.valueOf(analysisId) + ".sql");

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
	    	  log.error(String.format("Unable to translate sql for analysis %d", analysisId), e);
	      }
	     
	      if (sql != null) {
			  try {
				  results = getJdbcTemplate().query(sql, new RowMapper<Map<String, String>>(){

						@Override
						public Map<String, String> mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							Map<String, String> result = new LinkedHashMap<String, String>();
							ResultSetMetaData metaData = rs.getMetaData();
							int colCount = metaData.getColumnCount();
							for (int i = 1; i <= colCount; i++) {
								String columnLabel = metaData.getColumnLabel(i);
								String columnValue = String.valueOf(rs.getObject(i));
								result.put(columnLabel, columnValue);
							}
							return result;
						}
			            
			        });
				  
			  } catch (Exception e) {
		          log.error(String.format("Error for analysis %d with cohortDefinition=%s ", analysisId, id), e);
		      }
	      }
		  
		  return results;
	  }
}
 