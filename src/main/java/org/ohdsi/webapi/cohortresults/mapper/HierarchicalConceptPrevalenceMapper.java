package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.springframework.jdbc.core.RowMapper;

public class HierarchicalConceptPrevalenceMapper implements RowMapper<HierarchicalConceptRecord> {

	@Override
	public HierarchicalConceptRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HierarchicalConceptRecord record = new HierarchicalConceptRecord();
		record.setConceptId(rs.getLong("CONCEPT_ID"));
		record.setConceptPath(rs.getString("CONCEPT_PATH"));
		record.setNumPersons(rs.getLong("NUM_PERSONS"));
		record.setPercentPersons(rs.getDouble("PERCENT_PERSONS"));
		record.setPercentPersonsBefore(rs.getDouble("PERCENT_PERSONS_BEFORE"));
		record.setPercentPersonsAfter(rs.getDouble("PERCENT_PERSONS_AFTER"));
		record.setRiskDiffAfterBefore(rs.getDouble("RISK_DIFF_AFTER_BEFORE"));
		record.setLogRRAfterBefore(rs.getDouble("LOGRR_AFTER_BEFORE"));
                record.setCountValue(rs.getLong("COUNT_VALUE"));
		return record;
	}

}
