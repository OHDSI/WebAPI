package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.CumulativeObservationRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CumulativeObservationMapper implements RowMapper<CumulativeObservationRecord> {

	@Override
	public CumulativeObservationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		CumulativeObservationRecord record = new CumulativeObservationRecord();
		record.setSeriesName(rs.getString("SERIESNAME"));
		record.setxLengthOfObservation(rs.getInt("XLENGTHOFOBSERVATION"));
		record.setyPercentPersons(rs.getDouble("YPERCENTPERSONS"));
		return record;
	}

}
