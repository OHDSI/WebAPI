package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.webapi.paging.FacetValue;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.QueryModifiers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.ohdsi.webapi.util.QueryModifiers.groupBy;

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

    PreparedStatementRenderer psr = new MappedLookupStrategy(sourcecodes).prepareStatement(source,
            groupBy(countColumns(columnName), columnName));
    return jdbcTemplate.query(psr.getSql(), psr.getSetter(), getFacetRowMapper(columnName));
  }
}
