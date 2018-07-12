package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.webapi.model.FacetValue;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class MappedLookupFacetValues implements FacetValuesStrategy {

  private long[] sourcecodes;

  private Source source;

  private JdbcTemplate jdbcTemplate;

  public MappedLookupFacetValues(long[] sourcecodes, Source source, JdbcTemplate jdbcTemplate) {
    this.sourcecodes = sourcecodes;
    this.source = source;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<FacetValue> getFacetValues(String columnName) {

    String selectCols = columnName + ", count(" + columnName + ") as " + columnName + "_count ";
    PreparedStatementRenderer psr = new MappedLookupStrategy(sourcecodes).prepareStatement(source,
            sql -> "select " + selectCols + " from (" + sql + ") facets group by " + columnName);
    return jdbcTemplate.query(psr.getSql(), psr.getSetter(), getFacetRowMapper(columnName));
  }
}
