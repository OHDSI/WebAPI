package org.ohdsi.webapi.cdmresults.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.ohdsi.webapi.cdmresults.DescendantRecordCount;

public class DescendantRecordCountMapper {

	public DescendantRecordCount mapRow(ResultSet rs)
			throws SQLException {
		DescendantRecordCount descendantRecordCount = new DescendantRecordCount();
		descendantRecordCount.setId(rs.getInt("concept_id"));
		descendantRecordCount.setRecordCount(rs.getLong("record_count"));
		descendantRecordCount.setDescendantRecordCount(rs.getLong("descendant_record_count"));
		return descendantRecordCount;
	}
}
