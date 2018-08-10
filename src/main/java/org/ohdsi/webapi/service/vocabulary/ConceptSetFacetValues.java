package org.ohdsi.webapi.service.vocabulary;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.paging.FacetValue;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.QueryModifiers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.util.List;
import java.util.function.Function;

import static org.ohdsi.webapi.util.QueryModifiers.groupBy;

public class ConceptSetFacetValues implements FacetValuesStrategy {

  public static final String CONCEPT_SET_FIELDS = "CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID";

  public static final Function<String, String> conceptSetStatementFunction = sql -> sql.replaceAll("(?i)I\\.concept_id", "I.*")
          .replaceAll("(?i)select concept_id", "select concept.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)select c\\.concept_id", "select c.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)select distinct cr\\.concept_id_1 as concept_id", "select distinct cr.concept_id_1 as concept_id, "
                  + CONCEPT_SET_FIELDS.replaceAll("(?i)ISNULL\\(INVALID_REASON", "ISNULL(C.INVALID_REASON"))
          .replaceAll("(?i)where concept_id in", "where concept.concept_id in");

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
