package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HierarchicalConceptMapper implements RowMapper<HierarchicalConceptRecord> {

	@Override
	public HierarchicalConceptRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HierarchicalConceptRecord record = new HierarchicalConceptRecord();
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConceptPath(rs.getString("CONCEPT_PATH"));
		record.setRecordsPerPerson(rs.getDouble("RECORDS_PER_PERSON"));
		record.setNumPersons(rs.getLong("NUM_PERSONS"));
		record.setPercentPersons(rs.getDouble("PERCENT_PERSONS"));
		return record;
	}

}
