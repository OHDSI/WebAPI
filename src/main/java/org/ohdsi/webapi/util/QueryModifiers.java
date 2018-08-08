package org.ohdsi.webapi.util;

import org.ohdsi.webapi.service.vocabulary.ConceptSetFacetValues;
import org.ohdsi.webapi.source.SourceDaimon;

import java.util.function.Function;

public class QueryModifiers {

  public static Function<String, String> columnTable = sql -> sql.replaceAll(" concept_id", " CONCEPT.concept_id");

  public static Function<String, String> countWrapper(String countSql, String orderClause, String whereClause, String tableQualifier) {
    return sql -> "with included as (" + sql + "), counts as ( "
            + countSql + ") select c.concept_id, "
            + "ROW_NUMBER() over(ORDER BY " + orderClause + ") as rrow,"
            + ConceptSetFacetValues.CONCEPT_SET_FIELDS + ", counts.record_count, counts.descendant_record_count from "
            + tableQualifier
            + ".concept c join included on included.concept_id = c.concept_id join counts on cast(counts.concept_id as integer) = c.concept_id "
            + whereClause;
  }

  public static Function<String, String> noDistinct = sql -> sql.replaceAll("distinct ROW_NUMBER\\(\\)", "ROW_NUMBER()");

  public static Function<String, String> getWhereFunction(String whereClause) {

    return sql -> sql + " " + whereClause + "\n";
  }

  public static Function<String, String> countFunction = sql -> "select count(*) from ( " + sql + " ) Q";

}
