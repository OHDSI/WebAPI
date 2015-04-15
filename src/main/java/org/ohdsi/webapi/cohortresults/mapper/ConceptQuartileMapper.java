package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.ConceptQuartileRecord;
import org.springframework.jdbc.core.RowMapper;

public class ConceptQuartileMapper implements RowMapper<ConceptQuartileRecord> {

	@Override
	public ConceptQuartileRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptQuartileRecord record = new ConceptQuartileRecord();
		record.setCategory(rs.getString("CATEGORY"));
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setMaxValue(rs.getInt("MAX_VALUE"));
		record.setP75Value(rs.getInt("P75_VALUE"));
		record.setP10Value(rs.getInt("P10_VALUE"));
		record.setMedianValue(rs.getInt("MEDIAN_VALUE"));
		record.setP25Value(rs.getInt("P25_VALUE"));
		record.setMinValue(rs.getInt("MIN_VALUE"));
		record.setP90Value(rs.getInt("P90_VALUE"));
		return record;
	}

}
