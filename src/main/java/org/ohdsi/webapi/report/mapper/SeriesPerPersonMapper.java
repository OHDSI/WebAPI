package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.SeriesPerPerson;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

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
