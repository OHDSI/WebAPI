package org.ohdsi.webapi.util;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.model.PageRequest;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PageableUtils {

  public static String getOrderClause(PageRequest pageRequest) {

    return Arrays.stream(pageRequest.getOrder())
            .map(order -> pageRequest.getColumns()[order.getColumn()].getData() + " " + order.getDir())
            .collect(Collectors.joining(","));
  }

  public static String getSearchClause(PageRequest pageRequest) {

    String searchValue = pageRequest.getSearch().getValue();
    String result = "";
    if (StringUtils.isNotBlank(searchValue) && hasSearchableColumns(pageRequest)) {
      String searchValueStr = "'%" + searchValue.toLowerCase() + "%'";
      result += Arrays.stream(pageRequest.getColumns())
              .filter(PageRequest.Column::isSearchable)
              .map(column -> ("LOWER(CAST(" + column.getData() + " AS VARCHAR)) LIKE " + searchValueStr))
              .collect(Collectors.joining(" OR "));
    }
    return result;
  }

  public static boolean hasSearchableColumns(PageRequest pageRequest) {

    return Arrays.stream(pageRequest.getColumns()).anyMatch(PageRequest.Column::isSearchable);
  }

}
