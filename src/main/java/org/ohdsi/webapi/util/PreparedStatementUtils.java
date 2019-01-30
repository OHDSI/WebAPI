package org.ohdsi.webapi.util;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.source.Source;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PreparedStatementUtils {

  public static PreparedStatementCreator collapsePreparedStatementCreators(List<PreparedStatementCreator> creators) {
    if (!creators.stream().allMatch(c -> c instanceof PreparedStatementWithParamsCreator)) {
      throw new IllegalArgumentException("All items should be PreparedStatementWithParamsCreator");
    }
    StringBuilder sql = new StringBuilder();
    List<Object> orderedParamsList = new ArrayList<>();
    for(PreparedStatementCreator psc : creators) {
      PreparedStatementWithParamsCreator pswpc = (PreparedStatementWithParamsCreator) psc;
      sql.append(ensureEndsWithDotComma(pswpc.getSql())).append("\n");
      List<Object> paramsList = pswpc.getOrderedParamsList();
      if (Objects.nonNull(paramsList)) {
        orderedParamsList.addAll(paramsList);
      }
    }

    PreparedStatementSetter setter = new OrderedPreparedStatementSetter(orderedParamsList);

    return con -> {
      PreparedStatement statement = con.prepareStatement(sql.toString());
      setter.setValues(statement);
      return statement;
    };
  }

  public static List<PreparedStatementCreator> collapse(List<PreparedStatementCreator> creators, Source source) {
    if (Constants.REQUIRE_COLLAPSE_PS_DBMS.contains(source.getSourceDialect())) {
      return Collections.singletonList(collapsePreparedStatementCreators(creators));
    } else {
      return creators;
    }
  }

  public static void addAll(List<PreparedStatementCreator> targetList, List<PreparedStatementCreator> sourceList, Source source) {

    if (Constants.REQUIRE_COLLAPSE_PS_DBMS.contains(source.getSourceDialect())) {
      targetList.add(collapsePreparedStatementCreators(sourceList));
    } else {
      targetList.addAll(sourceList);
    }
  }

  public static String ensureEndsWithDotComma(String sql) {
    return sql.matches("(?m);\\w*$") ? sql : sql + ";";
  }
}
