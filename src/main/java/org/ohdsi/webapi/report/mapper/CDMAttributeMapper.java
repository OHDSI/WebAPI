package org.ohdsi.webapi.report.mapper;

import org.ohdsi.webapi.report.CDMAttribute;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CDMAttributeMapper implements RowMapper<CDMAttribute> {

	@Override
	public CDMAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
		CDMAttribute attribute = new CDMAttribute();
		attribute.setAttributeName(rs.getString("ATTRIBUTE_NAME"));
		attribute.setAttributeValue(rs.getString("ATTRIBUTE_VALUE"));
		return attribute;
	}

}
