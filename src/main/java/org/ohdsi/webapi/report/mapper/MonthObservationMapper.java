	package org.ohdsi.webapi.report.mapper;

    import org.ohdsi.webapi.report.MonthObservationRecord;
    import org.springframework.jdbc.core.RowMapper;

	import java.sql.ResultSet;
	import java.sql.SQLException;

	public class MonthObservationMapper implements RowMapper<MonthObservationRecord> {

        @Override
        public MonthObservationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            MonthObservationRecord record = new MonthObservationRecord();
            record.setMonthYear(rs.getInt("MONTHYEAR"));
            record.setPercentValue(rs.getDouble("PERCENTVALUE"));
            record.setCountValue(rs.getLong("COUNTVALUE"));
            return record;
        }

    }
