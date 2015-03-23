package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.SeriesPerPerson;
import org.springframework.jdbc.core.RowMapper;

public class SeriesPerPersonMapper implements RowMapper<SeriesPerPerson> {

	@Override
	public SeriesPerPerson mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		SeriesPerPerson record = new SeriesPerPerson();
		record.setSeriesName(rs.getString("SERIES_NAME"));
		record.setxCalendarMonth(rs.getInt("X_CALENDAR_MONTH"));
		record.setyRecordCount(rs.getInt("Y_RECORD_COUNT"));
		return record;
	}

}
