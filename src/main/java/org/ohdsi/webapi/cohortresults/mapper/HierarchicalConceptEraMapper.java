package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.springframework.jdbc.core.RowMapper;

public class HierarchicalConceptEraMapper implements RowMapper<HierarchicalConceptRecord> {

	@Override
	public HierarchicalConceptRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HierarchicalConceptRecord record = new HierarchicalConceptRecord();
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setLengthOfEra(rs.getDouble("LENGTH_OF_ERA"));
		record.setConceptPath(rs.getString("CONCEPT_PATH"));
		record.setNumPersons(rs.getLong("NUM_PERSONS"));
		record.setPercentPersons(rs.getDouble("PERCENT_PERSONS"));
		return record;
	}

}
