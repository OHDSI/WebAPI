package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.ScatterplotRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScatterplotMapper implements RowMapper<ScatterplotRecord> {

	@Override
	public ScatterplotRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ScatterplotRecord record = new ScatterplotRecord();
		record.setRecordType(rs.getString("RECORD_TYPE"));
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		record.setCountValue(rs.getInt("COUNT_VALUE"));
		record.setDuration(rs.getInt("DURATION"));
		record.setPctPersons(rs.getDouble("PCT_PERSONS"));
		return record;
	}

}
