package org.ohdsi.webapi.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.paging.PageRequest;
import org.ohdsi.webapi.vocabulary.ConceptAncestors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PageableUtils {

  public static Map<String, String> INCLUDED_CONCEPTS_FIELD_MAP = ImmutableMap.of("STANDARD_CONCEPT_CAPTION", "STANDARD_CONCEPT",
          "INVALID_REASON_CAPTION", "INVALID_REASON");
  public static Map<String, List<String>> STANDARD_CONCEPT_VALUES_MAP = ImmutableMap.of("Non-Standard", ImmutableList.of("N"),
          "Standard", ImmutableList.of("S"), "Classification", ImmutableList.of("C"));
  public static Map<String, List<String>> INVALID_REASON_VALUES_MAP = ImmutableMap.of("Valid", ImmutableList.of("V"),
          "Invalid", ImmutableList.of("D", "U"));
  public static Map<String, Map<String, List<String>>> INCLUDED_CONCEPT_VALUES_MAP = ImmutableMap.of("STANDARD_CONCEPT_CAPTION", STANDARD_CONCEPT_VALUES_MAP,
                  "INVALID_REASON_CAPTION", INVALID_REASON_VALUES_MAP);
  public static Set<String> INCLUDED_CONCEPTS_COUNTS_FIELDS = ImmutableSet.of("RECORD_COUNT", "DESCENDANT_RECORD_COUNT");
  public static Map<String, String> INCLUDED_CONCEPTS_COUNTS_SELECT = ImmutableMap.of("RECORD_COUNT", "ISNULL(max(c1.agg_count_value), 0) record_count",
                          "DESCENDANT_RECORD_COUNT", "ISNULL(sum(c2.agg_count_value), 0) descendant_record_count");
  public static Map<String, Predicate<ConceptAncestors>> CONCEPT_COUNT_PREDICATE_MAP = ImmutableMap.of("RECORD_COUNT", concept -> concept.recordCount > 0,
          "DESCENDANT_RECORD_COUNT", concept -> concept.descendantRecordCount > 0);

  public static String getOrderClause(PageRequest pageRequest) {

    return getOrderClause(pageRequest, true);
  }

  public static String getOrderClause(PageRequest pageRequest, boolean includeTablePrefix) {

    return Arrays.stream(pageRequest.getOrder())
            .map(order -> {
              String column = pageRequest.getColumns()[order.getColumn()].getData();
              StringBuilder sb = new StringBuilder();
              if (includeTablePrefix) {
                sb.append(getTablePrefix(column)).append(".");
              }
              return sb.append(column).append(" ").append(order.getDir()).toString();
            })
            .collect(Collectors.joining(","));
  }

  private static String getTablePrefix(String column) {

    return INCLUDED_CONCEPTS_COUNTS_FIELDS.stream().anyMatch(f -> f.equalsIgnoreCase(column)) ? "counts" : "c";
  }

  public static String getSearchClause(PageRequest pageRequest) {

    return getSearchClause(pageRequest, true);
  }

  public static String getSearchClause(PageRequest pageRequest, boolean includeTablePrefix) {

    String searchValue = pageRequest.getSearch().getValue();
    String result = "";
    if (StringUtils.isNotBlank(searchValue) && hasSearchableColumns(pageRequest)) {
      String searchValueStr = "'%" + searchValue.toLowerCase() + "%'";
      result += Arrays.stream(pageRequest.getColumns())
              .filter(PageRequest.Column::isSearchable)
              .map(column -> ("LOWER(CAST(" + (includeTablePrefix ? getTablePrefix(column.getData()) + "." : "")
                      + column.getData() + " AS VARCHAR)) LIKE " + searchValueStr))
              .collect(Collectors.joining(" OR "));
    }
    return result;
  }

  public static boolean hasSearchableColumns(PageRequest pageRequest) {

    return Arrays.stream(pageRequest.getColumns()).anyMatch(PageRequest.Column::isSearchable);
  }

}
