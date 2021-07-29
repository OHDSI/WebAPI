package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.model.results.AnalysisResults;
import org.springframework.jdbc.core.RowMapper;


/**
 *
 */
public class AnalysisResultsMapper implements RowMapper<AnalysisResults> {

    /**
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public AnalysisResults mapRow(ResultSet rs, int rowNum) throws SQLException {
        AnalysisResults ar = new AnalysisResults();
        ar.setAnalysisId(rs.getInt("ANALYSIS_ID"));
        ar.setCohortDefinitionId(rs.getInt("COHORT_DEFINITION_ID"));
        ar.setStratum1(rs.getString("STRATUM_1"));
        ar.setStratum2(rs.getString("STRATUM_2"));
        ar.setStratum3(rs.getString("STRATUM_3"));
        ar.setStratum4(rs.getString("STRATUM_4"));
        ar.setStratum5(rs.getString("STRATUM_5"));
        ar.setCountValue(rs.getInt("count_value"));

        return ar;
    }
    
}
