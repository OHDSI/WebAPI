package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.CohortAnalysis;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.model.results.Analysis;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Path("/cohortanalysis/")
@Component
public class CohortAnalysisService extends AbstractDaoService {
	
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
    }
    
    /**
     * Returns all cohort analyses in the results/OHDSI schema
     * 
     * @return
     */
    @GET
    @Path("/")
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
        sql = SqlRender.renderSql(sql, new String[] { "resultsSchema", "heraclesResultsTable", "cohortDefinitionId" }, 
        		new String[] { this.getOhdsiSchema(), this.getHeraclesResultsTable(), String.valueOf(id) });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
        
        return getJdbcTemplate().query(sql, this.cohortAnalysisMapper);
    }
}
