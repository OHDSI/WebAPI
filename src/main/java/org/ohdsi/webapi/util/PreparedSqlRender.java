package org.ohdsi.webapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class PreparedSqlRender {

  public static String removeSqlComments(String sql) {

    return sql.replaceAll("(--.*)", "").replaceAll("\\/\\*([\\S\\s]+?)\\*\\/", "");
  }

  public static String fixPreparedStatementSql(String sql, Map<String, Object> paramValueMap) {

    for (Map.Entry<String, Object> entry : paramValueMap.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof String || value instanceof Integer || value instanceof Long || value == null) {
        sql = sql.replace("'%@" + entry.getKey() + "%'", "?");
        sql = sql.replace("'@" + entry.getKey() + "'", "?");
        sql = sql.replace("@" + entry.getKey(), "?");

      } else if (entry.getValue() instanceof Object[]) {
        int length = ((Object[]) entry.getValue()).length;
        sql = sql.replace("@" + entry.getKey(), StringUtils.repeat("?", ",", length));
      }
    }
    return sql;
  }

  public static List<Object> getOrderedListOfParameterValues(Map<String, Object> paramValueMap, String sql) {

    List<Object> result = new ArrayList<>();
    String regex = "(@\\w+)|(%@\\w+%)";

    Pattern p = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
    Matcher matcher = p.matcher(sql);
    while (matcher.find()) {
      String group = matcher.group();
      String param = group.replace("@", "").replace(")", "").trim();//.toLowerCase();
      if (param.contains("%")) {
        param = param.replace("%", "");
        addToList(result, "%" + paramValueMap.get(param) + "%");
      } else {
        addToList(result, paramValueMap.get(param));
      }
    }
    return result;
  }

  private static void addToList(List<Object> result, Object value) {

    if (value instanceof String || value instanceof Integer || value instanceof Long || value == null) {
      result.add(value);
    } else if (value instanceof String[]) {
      result.addAll(Arrays.asList((String[]) value));
    } else if (value instanceof Long[]) {
      result.addAll(Arrays.asList((Long[]) value));
    } else if (value instanceof Integer[]) {
      result.addAll(Arrays.asList((Integer[]) value));
    } else if (value instanceof Object[]) {
      result.addAll(Arrays.asList((Object[]) value));
    }
  }
}

