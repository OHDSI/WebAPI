package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.ConceptDistributionRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConceptDistributionMapper implements RowMapper<ConceptDistributionRecord>{

	@Override
	public ConceptDistributionRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ConceptDistributionRecord record = new ConceptDistributionRecord();
		record.setCountValue(rs.getLong("COUNTVALUE"));
		record.setIntervalIndex(rs.getInt("INTERVALINDEX"));
		record.setPercentValue(rs.getDouble("PERCENTVALUE"));
		return record;
	}

}
