package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.Path;

import org.ohdsi.webapi.model.Cohort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * 
 * TODO replace all cohort_definition lookups with that service, once available
 *
 */
@Path("/cohort/")
@Component
public class CohortService extends AbstractDaoService {
    
    private final RowMapper<Cohort> cohortMapper = new RowMapper<Cohort>() {
        
        @Override
        public Cohort mapRow(final ResultSet rs, final int arg1) throws SQLException {
            final Cohort cohort = new Cohort();
            cohort.setCohortDefinitionId(rs.getInt(Cohort.COHORT_DEFINITION_ID));
            cohort.setCohortStartDate(rs.getDate(Cohort.COHORT_START_DATE));
            cohort.setCohortEndDate(rs.getDate(Cohort.COHORT_END_DATE));
            cohort.setSubjectId(rs.getInt(Cohort.SUBJECT_ID));
            return cohort;
        }
    };
}
