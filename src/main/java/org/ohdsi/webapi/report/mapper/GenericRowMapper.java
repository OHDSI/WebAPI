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

    @Override
    public JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            String capitalized = WordUtils.capitalizeFully(column, new char[]{'_'}).replaceAll("_", "");
            String fieldName = capitalized.substring(0, 1).toLowerCase() + capitalized.substring(1);
            if(fieldName.endsWith("pp")){
                fieldName = fieldName.replace("pp", "Pp");
            }
            Object value = rs.getObject(column);
            if (value == null) {
                objectNode.putNull(fieldName);
            } else if (value instanceof Integer) {
                objectNode.put(fieldName, (Integer) value);
            } else if (value instanceof String) {
                objectNode.put(fieldName, (String) value);
            } else if (value instanceof Boolean) {
                objectNode.put(fieldName, (Boolean) value);
            } else if (value instanceof Date) {
                objectNode.put(fieldName, ((Date) value).getTime());
            } else if (value instanceof Long) {
                objectNode.put(fieldName, (Long) value);
            } else if (value instanceof Double) {
                objectNode.put(fieldName, (Double) value);
            } else if (value instanceof Float) {
                objectNode.put(fieldName, (Float) value);
            } else if (value instanceof BigDecimal) {
                objectNode.put(fieldName, (BigDecimal) value);
            } else if (value instanceof Byte) {
                objectNode.put(fieldName, (Byte) value);
            } else if (value instanceof byte[]) {
                objectNode.put(fieldName, (byte[]) value);
            } else {
                throw new IllegalArgumentException("Unmappable object type: " + value.getClass());
            }
        }
        return objectNode;
    }
}
