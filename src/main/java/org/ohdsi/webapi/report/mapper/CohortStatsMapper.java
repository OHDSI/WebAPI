package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.CohortStatsRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CohortStatsMapper implements RowMapper<CohortStatsRecord> {

	@Override
	public CohortStatsRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		CohortStatsRecord stats = new CohortStatsRecord();
        stats.setIntervalSize(rs.getInt("INTERVALSIZE"));
        stats.setMaxValue(rs.getInt("MAXVALUE"));
        stats.setMinValue(rs.getInt("MINVALUE"));
        return stats;
	}

}
