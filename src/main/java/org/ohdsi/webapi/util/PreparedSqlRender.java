package org.ohdsi.webapi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.source.Source;

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
	
	// Given a source, determine how many parameters are allowed for IN clauses
	// when using prepared statements. This function will return -1 if there
	// is no known limit otherwise it will return the value based on the 
	// sourceDialect property of the source object
	public static int getParameterLimit(Source source) {
		int returnVal = -1;
		String sourceDialect = source.getSourceDialect().toLowerCase();
		
		if (sourceDialect.equals("oracle")) {
			returnVal = 990;
		} else if (sourceDialect.equals("sql server") || sourceDialect.equals("pdw")) {
			returnVal = 2000;
		} else if (sourceDialect.equals("postgresql") || sourceDialect.equals("redshift")) {
			returnVal = 30000;
		}
		return returnVal;
	}
}

