package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.PrevalenceRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PrevalanceConceptNameMapper implements RowMapper<PrevalenceRecord> {

	@Override
	public PrevalenceRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		PrevalenceRecord record = new PrevalenceRecord();
		record.setyPrevalence1000Pp(rs.getDouble("Y_PREVALENCE_1000PP"));
		record.setxCalendarMonth(rs.getInt("X_CALENDAR_MONTH"));
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		return record;
	}

}
