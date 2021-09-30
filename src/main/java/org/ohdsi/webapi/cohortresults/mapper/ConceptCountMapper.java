package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.ConceptCountRecord;
import org.springframework.jdbc.core.RowMapper;

public class ConceptCountMapper implements RowMapper<ConceptCountRecord> {

	@Override
	public ConceptCountRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptCountRecord record = new ConceptCountRecord();
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		record.setCountValue(rs.getLong("COUNT_VALUE"));
		return record;
	}

}
