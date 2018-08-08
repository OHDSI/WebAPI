package org.ohdsi.webapi.util;

import java.util.function.Function;

public class QueryModifiers {

  public static final String CONCEPT_SET_FIELDS = "CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID";
  public static final Function<String, String> conceptSetStatementFunction = sql -> sql.replaceAll("(?i)I\\.concept_id", "I.*")
          .replaceAll("(?i)select concept_id", "select concept.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)select c\\.concept_id", "select c.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)where concept_id in", "where concept.concept_id in");
  public static Function<String, String> columnTable = sql -> sql.replaceAll(" concept_id", " CONCEPT.concept_id");

  public static Function<String, String> countWrapper(String countSql, String orderClause, String whereClause, String tableQualifier) {
    return sql -> "with included as (" + sql + "), counts as ( "
            + countSql + ") select c.concept_id, "
            + "ROW_NUMBER() over(ORDER BY " + orderClause + ") as rrow,"
            + CONCEPT_SET_FIELDS + ", counts.record_count, counts.descendant_record_count from "
            + tableQualifier
            + ".concept c join included on included.concept_id = c.concept_id join counts on cast(counts.concept_id as integer) = c.concept_id "
            + whereClause;
  }

  public static Function<String, String> noDistinct = sql -> sql.replaceAll("distinct ROW_NUMBER\\(\\)", "ROW_NUMBER()");

  public static Function<String, String> getWhereFunction(String whereClause) {

    return sql -> sql + " " + whereClause + "\n";
  }

  public static Function<String, String> countFunction = sql -> "select count(*) from ( " + sql + " ) Q";

  public static Function<String, String> groupBy(String columns, String groupByCol) {
    return sql -> "select " + columns + " from (" + sql + ") facets group by " + groupByCol;
  }

}
