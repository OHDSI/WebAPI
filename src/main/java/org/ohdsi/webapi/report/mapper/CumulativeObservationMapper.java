package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.CumulativeObservationRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CumulativeObservationMapper implements RowMapper<CumulativeObservationRecord> {

	@Override
	public CumulativeObservationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		CumulativeObservationRecord record = new CumulativeObservationRecord();
		record.setSeriesName(rs.getString("SERIES_NAME"));
		record.setxLengthOfObservation(rs.getInt("X_LENGTH_OF_OBSERVATION"));
		record.setyPercentPersons(rs.getDouble("Y_PERCENT_PERSONS"));
		return record;
	}

}
