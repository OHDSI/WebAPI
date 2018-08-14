package org.ohdsi.webapi.util;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QueryModifiers {

  public static final String CONCEPT_SET_FIELDS = "CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, VOCABULARY_ID";
  public static final Function<String, String> conceptSetStatementFunction = sql -> sql.replaceAll("(?i)I\\.concept_id", "I.*")
          .replaceAll("(?i)select concept_id", "select concept.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)select c\\.concept_id", "select c.concept_id, " + CONCEPT_SET_FIELDS)
          .replaceAll("(?i)select distinct cr\\.concept_id_1 as concept_id", "select distinct cr.concept_id_1 as concept_id, "
                  + CONCEPT_SET_FIELDS.replaceAll("(?i)ISNULL\\(INVALID_REASON", "ISNULL(C.INVALID_REASON"))
          .replaceAll("(?i)where concept_id in", "where concept.concept_id in");
  public static Function<String, String> columnTable = sql -> sql.replaceAll("select concept_id", "select CONCEPT.concept_id");

  public static Supplier<String> identifiersToString(String[] identifiers) {
      return () -> Arrays.stream(identifiers).map(i -> String.valueOf(Integer.parseInt(i.replaceAll("'", ""))))
          .collect(Collectors.joining(","));
  }

  public static Function<String, String> countWrapper(String countSql, String orderClause, String whereClause, String tableQualifier) {
    return sql -> "with included as (" + sql + "), "
            + countSql.replaceFirst("(?i)SELECT\\s+concepts.ancestor_id", ", numbers as (select concepts.ancestor_id ") + "),\n "
            + "results as (select c.concept_id, "
            + "ROW_NUMBER() over(ORDER BY " + orderClause + ") as rrow,"
            + CONCEPT_SET_FIELDS + ", numbers.record_count, numbers.descendant_record_count from "
            + tableQualifier
            + ".concept c join included on included.concept_id = c.concept_id join numbers on cast(numbers.concept_id as integer) = c.concept_id "
            + whereClause + ")";
  }

  public static Function<String, String> noDistinct = sql -> sql.replaceAll("distinct ROW_NUMBER\\(\\)", "ROW_NUMBER()");

  public static Function<String, String> getWhereFunction(String whereClause) {

    return sql -> sql + " " + whereClause + "\n";
  }

  public static Function<String, String> countFunction = sql -> "select count(*) from ( " + sql + " ) Q";

  public static Function<String, String> noOrderBy = sql -> sql.replaceAll("(?i)order by .*$", "");

  public static Function<String, String> groupBy(String columns, String groupByCol) {
    return sql -> "select " + columns + " from (" + sql + ") facets group by " + groupByCol;
  }

}
