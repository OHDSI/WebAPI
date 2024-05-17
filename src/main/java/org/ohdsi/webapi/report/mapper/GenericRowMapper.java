package org.ohdsi.webapi.report.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.WordUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by mark on 10/29/16.
 */
public class GenericRowMapper implements RowMapper<JsonNode> {
    private final ObjectMapper mapper;

    public GenericRowMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectNode objectNode = mapper.createObjectNode();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            Object value = rs.getObject(column);
            column = this.snakeCaseToCamelCase(column);
            if (value == null) {
                objectNode.putNull(column);
            } else if (value instanceof Integer integer) {
                objectNode.put(column, integer);
            } else if (value instanceof String string) {
                objectNode.put(column, string);
            } else if (value instanceof Boolean boolean1) {
                objectNode.put(column, boolean1);
            } else if (value instanceof Date date) {
                objectNode.put(column, date.getTime());
            } else if (value instanceof Long long1) {
                objectNode.put(column, long1);
            } else if (value instanceof Double double1) {
                objectNode.put(column, double1);
            } else if (value instanceof Float float1) {
                objectNode.put(column, float1);
            } else if (value instanceof BigDecimal decimal) {
                objectNode.put(column, decimal);
            } else if (value instanceof Byte byte1) {
                objectNode.put(column, byte1);
            } else if (value instanceof byte[] bytes) {
                objectNode.put(column, bytes);
            } else {
                throw new IllegalArgumentException("Unmappable object type: " + value.getClass());
            }
        }
        return objectNode;
    }
		
    protected String snakeCaseToCamelCase(String str) {
        char[] delimeters = new char[] { '_' };
        str = WordUtils.capitalizeFully(str.toLowerCase(), delimeters)
            .replace("_", "");
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
