package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.webapi.model.FacetValue;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public interface FacetValuesStrategy {

  List<FacetValue> getFacetValues(String columnName);

  default RowMapper<FacetValue> getFacetRowMapper(String columnName) {
    return (rs, rowNum) -> {
      FacetValue value = new FacetValue();
      value.setValue(rs.getString(columnName));
      value.setCount(rs.getInt(columnName + "_count"));
      return value;
    };
  }

  default String countColumns(String columnName) {
    return columnName + ", count(" + columnName + ") as " + columnName + "_count ";
  }
}
