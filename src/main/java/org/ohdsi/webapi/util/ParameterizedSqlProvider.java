package org.ohdsi.webapi.util;

import org.springframework.jdbc.core.SqlProvider;

import java.util.List;

public interface ParameterizedSqlProvider extends SqlProvider {
  List<Object> getOrderedParamsList();
}
