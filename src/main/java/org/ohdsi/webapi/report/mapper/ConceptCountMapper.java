package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.ConceptCountRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConceptCountMapper implements RowMapper<ConceptCountRecord> {

	@Override
	public ConceptCountRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptCountRecord record = new ConceptCountRecord();
		record.setConceptId(rs.getLong("CONCEPTID"));
		record.setConceptName(rs.getString("CONCEPTNAME"));
		record.setCountValue(rs.getLong("COUNTVALUE"));
		return record;
	}

}
