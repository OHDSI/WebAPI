package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.CumulativeObservationRecord;
import org.springframework.jdbc.core.RowMapper;

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
