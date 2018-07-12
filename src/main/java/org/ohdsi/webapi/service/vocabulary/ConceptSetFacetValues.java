package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.model.FacetValue;
import org.ohdsi.webapi.source.Source;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.util.List;
import java.util.function.Function;

public class ConceptSetFacetValues implements FacetValuesStrategy {

  public static final String CONCEPT_SET_FIELDS = "CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID";

  public static final Function<String, String> conceptSetStatementFunction = sql -> sql.replaceAll("I\\.concept_id", "I.*")
          .replaceAll("select concept_id", "select concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("select c\\.concept_id", "select c.concept_id, " + CONCEPT_SET_FIELDS);

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

    String selectCols = columnName + ", count(" + columnName + ") as " + columnName + "_count ";
    String query = new ConceptSetStrategy(expression).prepareStatement(source,
            conceptSetStatementFunction.andThen(sql -> "select " + selectCols + " from (" + sql + ") facets group by " + columnName)).getSql();
    return jdbcTemplate.query(query, (PreparedStatementSetter)null, getFacetRowMapper(columnName));
  }
}
