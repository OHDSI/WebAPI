package org.ohdsi.webapi.cohortresults.mapper;

import org.ohdsi.webapi.cohortresults.ProfileSampleRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileSampleMapper implements RowMapper<ProfileSampleRecord> {
    @Override
    public ProfileSampleRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        ProfileSampleRecord record = new ProfileSampleRecord();
        record.setAgeGroup (rs.getInt("age_group"));
        record.setGenderConceptId(rs.getLong("gender_concept_id"));
        record.setPersonId(rs.getLong("person_id"));
        return record;
    }
}
