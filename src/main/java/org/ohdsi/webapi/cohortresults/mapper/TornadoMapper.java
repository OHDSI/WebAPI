package org.ohdsi.webapi.cohortresults.mapper;

import org.ohdsi.webapi.cohortresults.TornadoRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TornadoMapper implements RowMapper<TornadoRecord> {
    @Override
    public TornadoRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        TornadoRecord record = new TornadoRecord();
        record.setAgeGroup(rs.getInt("age_group"));
        record.setGenderConceptId(rs.getLong("gender_concept_id"));
        record.setPersonCount(rs.getLong("person_count"));
        return record;
    }
}
