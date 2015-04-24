package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.ConceptDecileRecord;
import org.springframework.jdbc.core.RowMapper;

public class ConceptDecileCountsMapper implements RowMapper<ConceptDecileRecord> {

	@Override
	public ConceptDecileRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptDecileRecord record = new ConceptDecileRecord();
		record.setTrellisName(rs.getString("TRELLIS_NAME"));
		record.setSeriesName(rs.getString("SERIES_NAME"));
		record.setyPrevalence1000Pp(rs.getDouble("Y_PREVALENCE_1000PP"));
		record.setxCalendarYear(rs.getInt("X_CALENDAR_YEAR"));
		record.setNumPersons(rs.getInt("NUM_PERSONS"));
		return record;
	}

}
