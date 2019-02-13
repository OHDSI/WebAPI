package org.ohdsi.webapi.util;

import org.springframework.jdbc.core.PreparedStatementCreator;

public interface PreparedStatementWithParamsCreator extends PreparedStatementCreator, ParameterizedSqlProvider {
}
