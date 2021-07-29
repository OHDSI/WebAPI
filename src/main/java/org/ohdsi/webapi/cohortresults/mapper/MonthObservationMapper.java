	package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.MonthObservationRecord;
import org.springframework.jdbc.core.RowMapper;

public class MonthObservationMapper implements RowMapper<MonthObservationRecord> {

	@Override
	public MonthObservationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		MonthObservationRecord record = new MonthObservationRecord();
		record.setMonthYear(rs.getInt("MONTH_YEAR"));
		record.setPercentValue(rs.getDouble("PERCENT_VALUE"));
		record.setCountValue(rs.getLong("COUNT_VALUE"));
		return record;
	}

}
