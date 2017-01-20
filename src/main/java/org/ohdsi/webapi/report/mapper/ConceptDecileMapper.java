package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.ConceptDecileRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConceptDecileMapper implements RowMapper<ConceptDecileRecord> {

	@Override
	public ConceptDecileRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptDecileRecord record = new ConceptDecileRecord();
		record.setTrellisName(rs.getString("TRELLIS_NAME"));
//		record.setConceptId(rs.getLong("CONCEPT_ID"));
        record.setSeriesName(rs.getString("SERIES_NAME"));
		record.setyPrevalence1000Pp(rs.getDouble("Y_PREVALENCE_1000PP"));
		record.setxCalendarYear(rs.getInt("X_CALENDAR_YEAR"));
		return record;
	}

}
