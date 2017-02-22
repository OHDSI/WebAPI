package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.cohortresults.ConceptCountRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConceptConditionCountMapper implements RowMapper<ConceptCountRecord> {

	@Override
	public ConceptCountRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ConceptCountRecord record = new ConceptCountRecord();
		record.setConditionConceptId(rs.getLong("CONDITION_CONCEPT_ID"));
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConditionConceptName(rs.getString("CONDITION_CONCEPT_NAME"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		record.setCountValue(rs.getLong("COUNT_VALUE"));
		return record;
	}

}
