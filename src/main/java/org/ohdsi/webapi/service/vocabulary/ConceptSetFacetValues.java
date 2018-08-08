package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.model.FacetValue;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.QueryModifiers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.util.List;

import static org.ohdsi.webapi.util.QueryModifiers.groupBy;

public class ConceptSetFacetValues implements FacetValuesStrategy {

  private ConceptSetExpression expression;

  private Source source;

  private JdbcTemplate jdbcTemplate;

  public ConceptSetFacetValues(ConceptSetExpression expression, Source source, JdbcTemplate jdbcTemplate) {
    this.expression = expression;
    this.source = source;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<FacetValue> getFacetValues(String columnName) {

    String query = new ConceptSetStrategy(expression).prepareStatement(source,
            QueryModifiers.conceptSetStatementFunction.andThen(groupBy(countColumns(columnName), columnName))).getSql();
    return jdbcTemplate.query(query, (PreparedStatementSetter)null, getFacetRowMapper(columnName));
  }
}
