package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.ConceptDistributionRecord;
import org.springframework.jdbc.core.RowMapper;

public class ConceptDistributionMapper implements RowMapper<ConceptDistributionRecord>{

	@Override
	public ConceptDistributionRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ConceptDistributionRecord record = new ConceptDistributionRecord();
		record.setCountValue(rs.getLong("COUNT_VALUE"));
		record.setIntervalIndex(rs.getInt("INTERVAL_INDEX"));
		record.setPercentValue(rs.getDouble("PERCENT_VALUE"));
		return record;
	}

}
