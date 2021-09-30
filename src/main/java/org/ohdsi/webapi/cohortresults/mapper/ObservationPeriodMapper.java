package org.ohdsi.webapi.cohortresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ohdsi.webapi.cohortresults.ObservationPeriodRecord;
import org.springframework.jdbc.core.RowMapper;

public class ObservationPeriodMapper implements RowMapper<ObservationPeriodRecord> {

	@Override
	public ObservationPeriodRecord mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ObservationPeriodRecord obsRecord = new ObservationPeriodRecord();
		obsRecord.setCohortDefinitionId(rs.getInt("COHORT_DEFINITION_ID"));
		obsRecord.setPctPersons(rs.getDouble("PCT_PERSONS"));
		obsRecord.setCountValue(rs.getInt("COUNT_VALUE"));
		obsRecord.setDuration(rs.getInt("DURATION"));
		return obsRecord;
	}

}
