package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.CohortStatsRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CohortStatsMapper implements RowMapper<CohortStatsRecord> {

	@Override
	public CohortStatsRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		CohortStatsRecord stats = new CohortStatsRecord();
		stats.setIntervalSize(rs.getInt("INTERVAL_SIZE"));
		stats.setMaxValue(rs.getInt("MAX_VALUE"));
		stats.setMinValue(rs.getInt("MIN_VALUE"));
		return stats;
	}

}
